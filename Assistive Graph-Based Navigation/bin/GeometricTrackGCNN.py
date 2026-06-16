import torch
import torch.nn.functional as F
try:
    from torch_geometric.nn import GCNConv, global_mean_pool
    from torch_geometric.data import Data, DataLoader
except ImportError:
    print("Please install torch-geometric")

import json
import os

# Helper to get paths relative to this script
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

def get_path(rel_path):
    return os.path.join(SCRIPT_DIR, rel_path)

# JAVADOC: This model represents the "Geometric Track" AI.
# It is much smaller than the standard model because it only has 4 input channels.
class GeometricTrackModel(torch.nn.Module):
    def __init__(self, in_channels=4, hidden_channels=32, out_channels=3):
        super().__init__()
        # JAVADOC: GCN Layers process the skeleton structure
        self.conv1 = GCNConv(in_channels, hidden_channels)
        self.conv2 = GCNConv(hidden_channels, hidden_channels)
        self.classifier = torch.nn.Linear(hidden_channels, out_channels)

    def forward(self, x, edge_index, edge_weight, batch):
        # 1. Node Embeddings
        x = self.conv1(x, edge_index, edge_weight).relu()
        x = self.conv2(x, edge_index, edge_weight).relu()
        # 2. Global Pooling (Convert node-features to a single graph-feature)
        x = global_mean_pool(x, batch)
        # 3. Final Classification
        return self.classifier(x)

def load_dataset(filename):
    if not os.path.exists(filename): return []
    with open(filename, 'r') as f:
        raw_data = json.load(f)
    
    pyg_list = []
    for item in raw_data:
        # The new format has a 'boxes' array per image
        if 'boxes' not in item: continue
        for box in item['boxes']:
            if not box['vertices']: continue
            
            # vertices: [[relX, relY, degree, distCenter], ...]
            x = torch.tensor(box['vertices'], dtype=torch.float)
            # category_ID is the object-level label (normalized to 0-indexed)
            y = torch.tensor([box['category_ID'] - 1], dtype=torch.long)
            
            edge_idx = []
            edge_w = []
            for e in box['edges']:
                # edges: [[srcIdx, tgtIdx, angle, distance], ...]
                edge_idx.append([int(e[0]), int(e[1])])
                edge_w.append(e[3]) # Use distance as weight
            
            if not edge_idx:
                edge_index = torch.empty((2, 0), dtype=torch.long)
                edge_weight = torch.empty((0,), dtype=torch.float)
            else:
                edge_index = torch.tensor(edge_idx, dtype=torch.long).t().contiguous()
                edge_weight = torch.tensor(edge_w, dtype=torch.float)
            
            pyg_list.append(Data(x=x, edge_index=edge_index, edge_attr=edge_weight, y=y))
    return pyg_list

def train():
    print("Loading datasets from jsons/ directory...")
    train_data = load_dataset(get_path('jsons/gcnn_train.json'))
    valid_data = load_dataset(get_path('jsons/gcnn_valid.json'))
    
    if not train_data:
        print(f"Error: No training data found in {get_path('jsons/gcnn_train.json')}. Run ExportingMain.java first.")
        return

    train_loader = DataLoader(train_data, batch_size=32, shuffle=True)
    valid_loader = DataLoader(valid_data, batch_size=32)
    
    model = GeometricTrackModel()
    optimizer = torch.optim.Adam(model.parameters(), lr=0.01)

    print(f"Starting Training (Train size: {len(train_data)}, Valid size: {len(valid_data)})...")
    for epoch in range(1, 101):
        model.train()
        total_loss = 0
        for data in train_loader:
            optimizer.zero_grad()
            out = model(data.x, data.edge_index, data.edge_attr, data.batch)
            loss = F.cross_entropy(out, data.y)
            loss.backward()
            optimizer.step()
            total_loss += loss.item()
            
        if epoch % 10 == 0:
            model.eval()
            correct = 0
            with torch.no_grad():
                for data in valid_loader:
                    out = model(data.x, data.edge_index, data.edge_attr, data.batch)
                    pred = out.argmax(dim=1)
                    correct += (pred == data.y).sum().item()
            acc = correct / len(valid_data) if valid_data else 0
            print(f"Epoch {epoch:03d}, Loss: {total_loss/len(train_loader):.4f}, Val Acc: {acc:.4f}")

    torch.save(model.state_dict(), get_path('geometric_model.pt'))
    print(f"Model saved to {get_path('geometric_model.pt')} (Testing data was never seen)")

def predict(json_path):
    model_path = get_path('geometric_model.pt')
    if not os.path.exists(model_path):
        print(f"Error: {model_path} not found. Train the model first.")
        return

    model = GeometricTrackModel()
    model.load_state_dict(torch.load(model_path, map_location=torch.device('cpu')))
    model.eval()
    
    if not os.path.exists(json_path): return
    with open(json_path, 'r') as f:
        raw_data = json.load(f)

    for item in raw_data:
        image_id = item.get('image_id', 0)
        if 'boxes' not in item: continue
        
        for box in item['boxes']:
            if not box['vertices']: continue
            
            x = torch.tensor(box['vertices'], dtype=torch.float)
            edge_idx = []
            edge_w = []
            for e in box['edges']:
                edge_idx.append([int(e[0]), int(e[1])])
                edge_w.append(e[3])
            
            if not edge_idx:
                edge_index = torch.empty((2, 0), dtype=torch.long)
                edge_weight = torch.empty((0,), dtype=torch.float)
            else:
                edge_index = torch.tensor(edge_idx, dtype=torch.long).t().contiguous()
                edge_weight = torch.tensor(edge_w, dtype=torch.float)
            
            with torch.no_grad():
                batch = torch.zeros(x.size(0), dtype=torch.long)
                out = model(x, edge_index, edge_weight, batch)
                pred = out.argmax(dim=1).item()
                print(f"RES:{image_id},{pred + 1},0,0,0,0")

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('--predict', type=str, help='Path to input graph JSON')
    args = parser.parse_args()
    
    if args.predict:
        predict(args.predict)
    else:
        train()
