# Tile Detector: Assistive Graph-Based Navigation System

Welcome to **Tile Detector**, a hybrid navigation system designed to detect and interpret tactile paving patterns using Graph Convolutional Neural Networks (GCNN). This project combines a Java-based desktop interface with a Python AI backend.

---

## 🛠 Prerequisites

Before starting, ensure you have the following installed:

1.  **Python**: (Recommended version: **3.10** or higher)
2.  **JavaFX SDK**: (Recommended version: **21.0.11**)
    *   Download from [Gluon](https://gluonhq.com/products/javafx/).
    *   Note your installation path (e.g., `C:\javafx-sdk-21.0.11\lib`).

---

## 🚀 Setup Instructions

### 1. Local Python Environment (Recommended)
If you wish to run the AI locally, follow these steps to set up the environment:

1.  **Navigate to the `src` folder**:
    ```cmd
    cd src
    ```
2.  **Create a Virtual Environment**:
    ```cmd
    python -m venv .venv
    ```
   # ***let it finish loading 

3.  **Install AI Dependencies**:
#   ** these might take 30 mins each to finish installing 
#   patience...patience... 
    ```cmd
    .\.venv\Scripts\python.exe -m pip install torch --index-url https://download.pytorch.org/whl/cpu
    .\.venv\Scripts\python.exe -m pip install torch-geometric numpy
    
    ```
4.  **Train the Model (If `geometric_model.pt` is missing)**:
    ```cmd

    .\.venv\Scripts\python.exe GeometricTrackGCNN.py

    ```
### ----IGNORE IF YOU DONT HAVE DOCKER
### 2. Docker Alternative (Fallback)
If you do not want to set up Python locally, the application can use Docker.

1.  **Navigate to the `src` folder**:
    ```cmd
    cd src
    ```
2.  **Build the Docker Image**:
    ```cmd
    docker build -t gcnn-navigation .
    ```
3.  **Run Training via Docker**:
    ```cmd
    docker run --rm -v "%cd%:/app" gcnn-navigation python GeometricTrackGCNN.py
    ```

---

## 🖥 Running the Application

You can run the application using one of the following methods. **Ensure your JavaFX SDK path is correctly set.**

### Option A: Using the `run_ui` script
1.  Navigate to the **root** folder of the project.
2.  Open `src/run_ui.bat` and ensure the `JFX_PATH` matches in the BATCH FILE MATCHES your local JavaFX installation.
3.  Run the script:
    ```cmd
    .\src\run_ui.bat
    ```


### Option B: Running the JAR directly (Root Folder)
## 📁 You should See Project Structure
- `src/`: Contains source code, databases, and AI scripts.
- `dist/`: Contains the compiled `.jar` application.
- `ss/`: Presentation and documentation materials.
- `bin/`: Compiled Java class files.
From the project root, execute the following command 

### Again
(adjust the path to your JavaFX-SDK):

```cmd


java --module-path "C:\javafx-sdk-21.0.11\lib" --add-modules javafx.controls,javafx.graphics,javafx.fxml,java.sql,java.desktop -jar dist/tactileDetector.jar


```


### Option C: CREATE JAVAFX project, setup buidpath & run configurations...Run  src/App/UI.java



---

---

## 📁 Project Structure
- `src/`: Contains source code, databases, and AI scripts.
- `dist/`: Contains the compiled `.jar` application.
- `ss/`: Presentation and documentation materials.
- `bin/`: Compiled Java class files.

---

## 📺 Video Presentation
[Watch the demo video on YouTube](https://youtu.be/dMtqbKz7Xk8)
