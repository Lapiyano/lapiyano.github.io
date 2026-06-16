import sqlite3
import json
import os

def create_database(json_path, db_name):
 
    conn = sqlite3.connect(db_name)
    cursor = conn.cursor()

  
    cursor.executescript('''
        CREATE TABLE IF NOT EXISTS categories (
            id INTEGER PRIMARY KEY,
            name TEXT
        );

        CREATE TABLE IF NOT EXISTS images (
            id INTEGER PRIMARY KEY,
            file_name TEXT,
            width INTEGER,
            height INTEGER
        );

        CREATE TABLE IF NOT EXISTS annotations (
            id INTEGER PRIMARY KEY,
            image_id INTEGER,
            category_id INTEGER,
            bbox_x REAL,
            bbox_y REAL,
            bbox_w REAL,
            bbox_h REAL,
            FOREIGN KEY (image_id) REFERENCES images(id),
            FOREIGN KEY (category_id) REFERENCES categories(id)
        );

        CREATE TABLE IF NOT EXISTS segmentations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            annotation_id INTEGER,
            coords TEXT, -- Stored as a string of comma-separated numbers
            FOREIGN KEY (annotation_id) REFERENCES annotations(id)
        );
    ''')

    with open(json_path, 'r') as f:
        data = json.load(f)


    for cat in data['categories']:
        cursor.execute("INSERT OR IGNORE INTO categories VALUES (?, ?)", (cat['id'], cat['name']))

  
    for img in data['images']:
        cursor.execute("INSERT OR IGNORE INTO images VALUES (?, ?, ?, ?)", 
                       (img['id'], img['file_name'], img['width'], img['height']))

    
    for ann in data['annotations']:
 
        bbox = ann['bbox']
        cursor.execute("""
            INSERT INTO annotations (id, image_id, category_id, bbox_x, bbox_y, bbox_w, bbox_h) 
            VALUES (?, ?, ?, ?, ?, ?, ?)""", 
            (ann['id'], ann['image_id'], ann['category_id'], bbox[0], bbox[1], bbox[2], bbox[3]))
        
        
        for poly in ann['segmentation']:
            coord_string = ",".join(map(str, poly))
            cursor.execute("INSERT INTO segmentations (annotation_id, coords) VALUES (?, ?)", 
                           (ann['id'], coord_string))

    conn.commit()
    print(f"Database {db_name} created successfully!")
    conn.close()

create_database('src/Dataset/valid/_annotations.coco.json', 'src/tactile_valid.db')