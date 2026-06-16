# Lightweight Python image
FROM python:3.10-slim

# System dependencies (needed for building some Python packages)
RUN apt-get update && apt-get install -y \
    build-essential \
    git \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Install PyTorch (CPU version)
RUN pip install --no-cache-dir torch --index-url https://download.pytorch.org/whl/cpu

# Install PyTorch Geometric dependencies (IMPORTANT: must match torch version)
RUN pip install --no-cache-dir torch-scatter torch-sparse torch-cluster torch-spline-conv \
    -f https://data.pyg.org/whl/torch-$(python -c "import torch; print(torch.__version__)").html

# Install main PyG package
RUN pip install --no-cache-dir torch-geometric

# Copy project files into container
COPY . /app

# Run script
CMD ["python", "GeometricTrackGCNN.py"]

#  1 docker build -t gcnn-navigation .
#   This creates a "disk image" called gcnn-navigation that has a perfect Python environment.

#    1 docker run --rm -v %cd%:/app gcnn-navigation python GeometricTrackGCNN.py
#   (Note: %cd% is the CMD shortcut for the current folder path).