
package app;
 
//JavaFX imports 
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;
 
//Project imports
import gui.Components.View;              
import gui.Components.CustomButton;      
import gui.Components.CustomTextArea;    
import utils.CustomColours;              
import utils.NavCommand; 
import utils.NavHelper; 
import utils.UIStyles;
import visualise.visualise;              
import Extraction.Extraction;            
import Connections.dbLoader;          
import database.models.Images;
import database.models.annotations;
import javafx.scene.control.cell.PropertyValueFactory;
import Analysis.KnnSimilarityDetector;
import export.modelExports;

//Standard library 
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
* Main UI class for the Assisted Tactile Navigation System.
* 
* This class handles:
* Side bar navigation
* Home screen display
* Classification and similarity screens
* Application layout and styling
* 
* Design follows a dark theme with teal and blue accents.
*/
public class UI extends Application {

    // Helper class for TableView rows
    public static class SimilarityRow {
        private final String rank;
        private final String boxB;
        private final String boxA;
        private final String catB;
        private final String catA;
        private final String similarity;
        private final String match;

        public SimilarityRow(int rank, String boxB, String boxA, String catB, String catA, double sim, boolean isMatch) {
            this.rank = String.valueOf(rank);
            this.boxB = boxB;
            this.boxA = boxA;
            this.catB = catB;
            this.catA = catA;
            this.similarity = String.format("%.1f%%", sim);
            this.match = isMatch ? "✓" : "✗";
        }

        public String getRank() { return rank; }
        public String getBoxB() { return boxB; }
        public String getBoxA() { return boxA; }
        public String getCatB() { return catB; }
        public String getCatA() { return catA; }
        public String getSimilarity() { return similarity; }
        public String getMatch() { return match; }
    }

 //Database paths.
 private static final String DB_TRAIN = "src/tactile_train.db";
 private static final String DB_VALID = "src/tactile_valid.db";
 private static final String DB_TEST  = "src/tactile_test.db";

 //State.
 private dbLoader currentLoader = new dbLoader(DB_TRAIN);
 private KnnSimilarityDetector knnDetector = new KnnSimilarityDetector();
 private File     classFile  = null;
 private Image    classImage = null;
 private File     simFileA   = null;
 private File     simFileB   = null;
 private Image    simImageA  = null;
 private Image    simImageB  = null;
 private KnnSimilarityDetector.ImageEntry simEntryA = null;
 private KnnSimilarityDetector.ImageEntry simEntryB = null;

 //Layout handles. 
 private BorderPane root;
 private StackPane  contentArea;
 private TextArea   logArea;
 private Button     btnNavClass,btnNavSim,btnHome;

 //Home animation.
 private Canvas homeAnimCanvas;
 private double animT = 0;

 
 /**
  * Entry point for the JavaFX application.
  *
  * @param stage primary stage for the UI.
  */
 @Override
 public void start(Stage stage) {
    root        = new BorderPane();
    contentArea = new StackPane();

    root.setLeft(buildSidebar());
    root.setCenter(contentArea);
    buildLog();
    root.setBottom(logArea);
    root.setStyle(UIStyles.mainBackground());

    showHome();

    Scene scene = new Scene(root, 1300, 860);
    stage.setTitle("Assisted Tactile Navigation System");
    stage.setMinWidth(900);
    stage.setMinHeight(640);
    stage.setScene(scene);
    stage.show();

    log("[SYS] Application started. Select a mode from the sidebar.");

    // Load KNN data in background
    new Thread(() -> {
        log("[SIM] Loading KNN dataset features...");
        knnDetector.loadData();
        log("[SIM] KNN Detector ready. Loaded " + knnDetector.getAllImages().size() + " images.");
    }, "knn-loader").start();
 }



 //=========================================================
 // SIDEBAR
 // ========================================================

 /**
  * Builds the side bar containing navigation buttons and database selector.
  *
  * @return VBox representing the side bar layout.
  */
 private VBox buildSidebar() {
     VBox sidebar = new VBox(0);
     sidebar.setPrefWidth(258);
     sidebar.setStyle(UIStyles.sidebar() );

     //Logo block.
     VBox logoBlock = new VBox(6);
     logoBlock.setPadding(new Insets(28, 20, 24, 20));
     Label appLine1 = new Label("Assisted Tactile");
     appLine1.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
     appLine1.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     Label appLine2 = new Label("Navigation System");
     appLine2.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
     appLine2.setTextFill(Color.web(CustomColours.TEAL.getValue()));
     Label appLine3 = new Label("Graph-Based · Tactile Paving");
     appLine3.setFont(Font.font("Courier New",FontWeight.BOLD, 11));
     appLine3.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     
     //Side bar 3 lines. 
     VBox titleStack = new VBox(2, appLine1, appLine2, appLine3);
     HBox logoRow = new HBox(titleStack);
     titleStack.setAlignment(Pos.CENTER);
     logoRow.setAlignment(Pos.CENTER_LEFT);
     logoBlock.getChildren().add(logoRow);
     
     //separator Line.
     Region div1 = hLine();

     Label sectionLbl = new Label("ANALYSIS MODES");
     sectionLbl.setFont(Font.font("Courier New",FontWeight.BOLD, 12));
     sectionLbl.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     sectionLbl.setPadding(new Insets(14, 20, 8, 20));

     //Navigation buttons.
     btnHome     = navBtn("  Home");
     btnNavClass = navBtn("  Classification");
     btnNavSim   = navBtn("  Similarity Detection");

     //Side bar buttons to show corresponding task /home.
     btnHome.setOnAction(e -> { activateNav(btnHome); showHome(); });
     btnNavClass.setOnAction(e -> { activateNav(btnNavClass); showClassification(); });
     btnNavSim.setOnAction(e -> { activateNav(btnNavSim); showSimilarity(); });
     activateNav(btnHome);
     
     // Export button
     Button btnExport = navBtn("  Export & Reload Data");
     btnExport.setOnAction(e -> {
         btnExport.setDisable(true);
         log("[SYS] Exporting datasets to JSON...");
         new Thread(() -> {
             try {
                 modelExports export = new modelExports();
                 String[] types = {"test", "train", "valid"};
                 for (String type : types) {
                     String json = export.extractToJson(type);
                     try (FileWriter file = new FileWriter("src/jsons/gcnn_" + type + ".json")) {
                         file.write(json);
                         log("[SYS] Exported: gcnn_" + type + ".json");
                     }
                 }
                 log("[SYS] Reloading KNN Similarity Detector...");
                 knnDetector.reset();
                 knnDetector.loadData();
                 log("[SYS] Data reload complete. Ready.");
                 Platform.runLater(() -> {
                     btnExport.setDisable(false);
                 });
             } catch (Exception ex) {
                 log("[ERR] Export failed: " + ex.getMessage());
                 Platform.runLater(() -> {
                     btnExport.setDisable(false);
                 });
             }
         }, "export-thread").start();
     });
     
     //Creating a flexible space .
     Region spacer = new Region();
     VBox.setVgrow(spacer, Priority.ALWAYS);

     //DB selector.
     VBox dbBox = new VBox(7);
     dbBox.setPadding(new Insets(14, 20, 20, 20));
     Label dbLbl = new Label("DATABASE");
     dbLbl.setFont(Font.font("Courier New",FontWeight.BOLD, 10));
     dbLbl.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     
     //Database drop down.
     ComboBox<String> dbCombo = new ComboBox<>();
     dbCombo.getItems().addAll("Train DB", "Validation DB", "Test DB");
     dbCombo.setValue("Train DB");
     dbCombo.setMaxWidth(Double.MAX_VALUE);
     dbCombo.setStyle(UIStyles.comboBox());
     dbCombo.setOnAction(e -> {
         String v = dbCombo.getValue();
         currentLoader = new dbLoader(
         v.equals("Train DB") ? DB_TRAIN :
         v.equals("Validation DB") ? DB_VALID : DB_TEST);
         log("[DB] Switched to: " + v);
     });
     dbBox.getChildren().addAll(dbLbl, dbCombo);

     sidebar.getChildren().addAll(
         logoBlock, div1, sectionLbl,
         btnHome, btnNavClass, btnNavSim, btnExport,
         spacer, hLine(), dbBox
     );
     return sidebar;
 }

 
 /**
  * Creates a styled navigation button.
  *
  * @param text button label.
  * @return styled Button.
  */
 private Button navBtn(String text) {
     Button btn = new Button(text);
     btn.setMaxWidth(Double.MAX_VALUE);
     btn.setAlignment(Pos.CENTER_LEFT);
     btn.setPadding(new Insets(13, 22, 13, 22));
     btn.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
     btn.setCursor(Cursor.HAND);
     NavHelper.setInactive(btn);

     // Hover effect (ONLY if not active).
     btn.setOnMouseEntered(e -> { if (!isNavActive(btn)) btn.setStyle(UIStyles.navHover()); });
     btn.setOnMouseExited(e -> { if (!isNavActive(btn)) NavHelper.setInactive(btn); });
     
     return btn;
 }
 
 

 /**
  * Checks if a navigation button is active.
  *
  * @param b button to check.
  * @return true if active,false otherwise.
  */
 private boolean isNavActive(Button b) {
     return b.getStyle().contains(CustomColours.BLUE_NAV.getValue());
 }

 
 /**
  * Activates the given navigation button and deactivates others.
  *
  * @param active button to activate
  */
 private void activateNav(Button active) {
	 for (Button b : new Button[]{btnHome, btnNavClass, btnNavSim}) {
	        if (b != null) {
	        if (b == active) {
	         //Active style.
	         NavHelper.setActive(b);     
	         } else {
	         //Inactive style.
	         NavHelper.setInactive(b);   
	        }
	        }
	    }
 }

 
  //=========================
 // HOME SCREEN
 // =========================

 /**
  * Displays the Home screen with background image, overlay text, and fade-in animation.
  */
 private void showHome() {

 	//Background Image.
 	Image bgImage = new Image(new File("src/images/image2.jpg").toURI().toString());
    ImageView bgView = new ImageView(bgImage);
 	bgView.setPreserveRatio(false);
 	// Make image fill entire screen.
 	bgView.fitWidthProperty().bind(contentArea.widthProperty());
 	bgView.fitHeightProperty().bind(contentArea.heightProperty());

 	//Dark overlay (so text is readable).
 	Rectangle overlayShade = new Rectangle();
 	overlayShade.widthProperty().bind(contentArea.widthProperty());
 	overlayShade.heightProperty().bind(contentArea.heightProperty());
 	overlayShade.setFill(Color.web("#000000", 0.5)); // 50% dark

     //Overlay content, the VBox next to the navigation bar.
     VBox overlay = new VBox(26);
     overlay.setAlignment(Pos.CENTER);
     overlay.setPadding(new Insets(50, 80, 50, 80));
     overlay.setMaxWidth(680);

     //Badge.
     Label badge = new Label(" MINI PROJECT  ");
     badge.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
     badge.setTextFill(Color.web(CustomColours.TEAL.getValue()));
     badge.setPadding(new Insets(5, 14, 5, 14));
     badge.setStyle(UIStyles.badge());

     //Main title.
     Label title = new Label("Assistive Graph-Based\nNavigation System");
     title.setFont(Font.font("Courier New", FontWeight.BOLD, 36));
     title.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     title.setTextAlignment(TextAlignment.CENTER);
     title.setAlignment(Pos.CENTER);
     title.setLineSpacing(4);
     title.setEffect(new DropShadow(22, Color.web(CustomColours.TEAL.getValue(), 0.4)));

     //Teal accent bar under title.
     Rectangle bar = new Rectangle(110, 3, Color.web(CustomColours.TEAL.getValue()));

     //Description.
     Label desc = new Label(
         "Every day, visually impaired individuals rely on tactile paving\n" +
         "to navigate public spaces independently. This system analyses\n" +
         "ground images using graph-based computer vision identifying\n" +
         "safe corridors, corners, obstacles and stairs ahead.\n\n" +
         "Sobel edge detection extracts features. An AdjacencyListGraph\n" +
         "encodes spatial relationships for classification and similarity."
     );
     desc.setFont(Font.font("Courier New",FontWeight.BOLD, 12));
     desc.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     desc.setTextAlignment(TextAlignment.CENTER);
     desc.setAlignment(Pos.CENTER);
     desc.setLineSpacing(3);
     desc.setWrapText(true);

     // Feature pills
     HBox pills = new HBox(10);
     pills.setAlignment(Pos.CENTER);
     for (String f : new String[]{"Sobel Edges", "AdjacencyListGraph",
                                   "Classification", "Similarity"}) {
         Label p = new Label(f);
         p.setFont(Font.font("Courier New",FontWeight.BOLD, 11));
         p.setTextFill(Color.web(CustomColours.TEAL.getValue()));
         p.setPadding(new Insets(4, 12, 4, 12));
         p.setStyle(UIStyles.pill());
         pills.getChildren().add(p);
     }

     //CTA.
     Label cta = new Label("Select a mode from the sidebar to begin!");
     cta.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
     cta.setTextFill(Color.web(CustomColours.WHITE.getValue()));

     overlay.getChildren().addAll(badge, title, bar, desc, pills, cta);

     FadeTransition ft = new FadeTransition(Duration.millis(600), overlay);
     ft.setFromValue(0); ft.setToValue(1); ft.play();

     contentArea.getChildren().setAll(bgView, overlayShade, overlay);
     contentArea.setStyle(UIStyles.mainBackground());
 }

 
 /** 
  * Draws one frame of the animated home background (perspective paving lines + nodes) 
  * */
 
 //Fix/ Use on a later stage (project it finished).
 private void drawHomeFrame(Canvas c) {
     GraphicsContext gc = c.getGraphicsContext2D();
     double w = c.getWidth(), h = c.getHeight();

     // Fading trail effect
     gc.setFill(Color.web(CustomColours.BG_DARK.getValue(), 0.20));
     gc.fillRect(0, 0, w, h);

     // Vanishing point
     double vpX = w / 2, vpY = h * 0.30;

     // Perspective paving strip lines radiating from VP
     int nLines = 16;
     for (int i = 0; i < nLines; i++) {
         double phase    = animT * 0.55 + i * 0.38;
         double progress = phase % 1.0;
         double spread   = (w * 0.38) * progress;
         double x2 = vpX + (i - nLines / 2.0) * spread / (nLines / 2.0);
         double y2 = vpY + (h - vpY) * progress;
         double alpha = progress * 0.5;
         double lw    = 0.4 + progress * 2.2;
         Color col = (i % 3 == 0)
             ? Color.web(CustomColours.TEAL.getValue(), alpha)
             : Color.web(CustomColours.BLUE_LIGHT.getValue(), alpha * 0.55);
         gc.setStroke(col);
         gc.setLineWidth(lw);
         gc.strokeLine(vpX, vpY, x2, y2);
     }

     // Floating graph nodes
     for (int i = 0; i < 20; i++) {
         double nx = w * (0.15 + 0.7 * ((Math.sin(animT * 0.38 + i * 1.7) + 1) / 2.0));
         double ny = h * (0.18 + 0.62 * ((Math.cos(animT * 0.28 + i * 2.1) + 1) / 2.0));
         double a2 = 0.12 + 0.22 * Math.abs(Math.sin(animT + i));
         gc.setFill(Color.web(CustomColours.TEAL.getValue(), a2));
         gc.fillOval(nx - 3, ny - 3, 6, 6);
     }

     // Soft radial glow at vanishing point
     RadialGradient glow = new RadialGradient(0, 0, vpX, vpY, 110, false,
         CycleMethod.NO_CYCLE,
         new Stop(0, Color.web(CustomColours.TEAL.getValue(), 0.07)),
         new Stop(1, Color.TRANSPARENT));
     gc.setFill(glow);
     gc.fillOval(vpX - 110, vpY - 110, 220, 220);
 }

 
 

 // =========================
 // CLASSIFICATION SCREEN
 // =========================

 /**
  * Displays the Classification screen.
  * Allows user to select an image.
  */
 private void showClassification() {
     classFile = null; classImage = null;

     //Header
     Label title = screenTitle("  Classification");
     Label desc = grayLabel(
         "Select a dataset image. The system applies Sobel edge detection, " +
         "builds an AdjacencyListGraph, and classifies the tactile paving pattern " +
         "to derive a navigation command for the visually impaired user."
     );
     desc.setFont(Font.font("Courier New",FontWeight.BOLD, 11));
     VBox header = new VBox(10, title, hLine(), desc);
     header.setPadding(new Insets(30, 40, 0, 40));

     //Image display area.
     StackPane imageHolder = new StackPane();
     imageHolder.setMinHeight(360);
     imageHolder.setMaxHeight(440);
     imageHolder.setStyle(UIStyles.imageHolder());
     imageHolder.getChildren().add(emptySlotInner("Select an analysis image"));

     VBox colorIndicator = buildColorIndicator();
     colorIndicator.setVisible(false);
     colorIndicator.setMinWidth(220);

     HBox imageContainer = new HBox(20, imageHolder, colorIndicator);
     imageContainer.setAlignment(Pos.CENTER);
     HBox.setHgrow(imageHolder, Priority.ALWAYS);

     Label fileLabel = new Label("No image selected");
     desc.setFont(Font.font("Courier New",FontWeight.BOLD, 11));
     fileLabel.setFont(Font.font("Courier New", 11));
     fileLabel.setTextFill(Color.web(CustomColours.WHITE.getValue()));

     //Result panel (hidden until classify runs).
     VBox resultPanel = new VBox(14);
     resultPanel.setVisible(false);
     resultPanel.setStyle(UIStyles.imageHolder());
     resultPanel.setPadding(new Insets(22));

     //File chooser.
     FileChooser fc = new FileChooser();
     fc.setInitialDirectory(new File("src/Dataset/test"));
     fc.getExtensionFilters().add(
         new FileChooser.ExtensionFilter("Images","*.png","*.jpg","*.jpeg","*.bmp"));

     //Select image button (CustomButton - Blue).
     CustomButton btnSelect = new CustomButton("  Select Analysis Image", CustomColours.BUTTON_BLUE);
     btnSelect.setMaxWidth(280);

     // CLASSIFY IMAGE button (CustomButton - Green)
     CustomButton btnClassify = new CustomButton("  Classify Image", CustomColours.BUTTON_GREEN);
     btnClassify.setMaxWidth(280);
     btnClassify.setDisable(true);

     //Select image action.
     btnSelect.setOnAction(e -> {
         File f = fc.showOpenDialog(null);
         if (f == null) return;
         classFile = f;
         classImage = new Image(f.toURI().toString());

         //Display the image with bbox overlay
         StackPane display = buildImageDisplay(classImage, f, 860, 420);
         imageHolder.getChildren().setAll(display);
         fileLabel.setText("  " + f.getName());
         resultPanel.setVisible(false);
         colorIndicator.setVisible(true);
         btnClassify.setDisable(false);
         log("[CLF] Image loaded: " + f.getName());
     });

     //CLASSIFY IMAGE action — runs Extraction on a background thread.
     btnClassify.setOnAction(e -> {
         if (classImage == null) return;
         btnClassify.setDisable(true);
         resultPanel.setVisible(false);
         log("[CLF] Starting classification...");

         List<annotations> anns = fetchAnnotationsFor(classFile);

         new Thread(() -> {
             try {
                 // Run Sobel edge detection + graph construction
                 Extraction ext = new Extraction(classImage, anns);
                 ext.extract();

                 // Real GCNN Classification
                 Analysis.GcnnClassifier classifier = new Analysis.GcnnClassifier();
                 classifier.setLogger(msg -> log(msg));
                 List<Analysis.GcnnClassifier.Prediction> preds = classifier.classify(ext.graphs, anns);

                 // For backward compatibility and status, we still compute heuristic
                 int nodes = 0, edges = 0;
                 for (var g : ext.graphs) {
                     var vi = g.Vertices(); while (vi.hasNext()) { vi.next(); nodes++; }
                     var ei = g.edges();    while (ei.hasNext()) { ei.next(); edges++; }
                 }
                 NavCommand cmd   = deriveNavCommand(nodes, edges);
                 double conf      = heuristicConfidence(nodes, edges);
                 final int fn = nodes, fe = edges;

                 Platform.runLater(() -> {
                     // Update visualise canvas with colored results and labels
                     visualise v = null;
                     for (var node : imageHolder.getChildren()) {
                         if (node instanceof visualise) {
                             v = (visualise) node;
                             break;
                         } else if (node instanceof StackPane) {
                             for (var subNode : ((StackPane) node).getChildren()) {
                                 if (subNode instanceof visualise) {
                                     v = (visualise) subNode;
                                     break;
                                 }
                             }
                         }
                         if (v != null) break;
                     }

                     if (v != null) {
                         v.drawResults(anns, preds, ext.graphs);
                     }

                     // Fill result panel with AI results
                     fillClassResult(resultPanel, preds, conf, cmd, fn, fe);
                     resultPanel.setVisible(true);

                     btnClassify.setDisable(false);
                     log("[CLF] GCNN Prediction complete. " + preds.size() + " objects classified.");
                 });
             } catch (Exception ex) {
                 Platform.runLater(() -> {
                     log("[ERR] Classification failed: " + ex.getMessage());
                     btnClassify.setDisable(false);
                 });
             }
         }, "clf-thread").start();
     });

     HBox btnRow = new HBox(14, btnSelect, btnClassify);
     btnRow.setAlignment(Pos.CENTER_LEFT);
     btnRow.setPadding(new Insets(0, 40, 0, 40));

     VBox screen = new VBox(18,
         header,
         padH(imageContainer, 40),
         padH(fileLabel, 40),
         btnRow,
         padH(resultPanel, 40)
     );
     screen.setPadding(new Insets(0, 0, 30, 0));
     screen.setStyle(UIStyles.mainBackground());

     ScrollPane sp = scrollWrap(screen);
     contentArea.getChildren().setAll(sp);
     fadeIn(sp);
 }

 /** Populates the classification result card */
 private void fillClassResult(VBox panel, List<Analysis.GcnnClassifier.Prediction> preds,
                               double conf, NavCommand cmd, int nodes, int edges) {
     panel.getChildren().clear();

     panel.getChildren().add(sectionHead("GCNN CLASSIFICATION RESULTS"));

     for (int i = 0; i < preds.size(); i++) {
         Analysis.GcnnClassifier.Prediction p = preds.get(i);
         Label resultLbl = new Label("Object " + (i + 1) + ": " + p.categoryName);
         resultLbl.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
         resultLbl.setTextFill(Color.web(p.color));
         resultLbl.setEffect(new DropShadow(10, Color.web(p.color, 0.4)));
         panel.getChildren().add(resultLbl);
     }

     panel.getChildren().add(hLine());
     
     Label navLbl = new Label("Navigation Suggestion: " + cmd.name());
     navLbl.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
     navLbl.setTextFill(Color.web(CustomColours.TEAL.getValue()));

     // Confidence bar
     Label confPct = new Label(String.format("%.1f%%", conf * 100));
     confPct.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
     confPct.setTextFill(Color.web(CustomColours.TEAL.getValue()));
     Label confLbl = new Label("HEURISTIC CONFIDENCE");
     confLbl.setFont(Font.font("Courier New", 9));
     confLbl.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     HBox confRow = new HBox(8, confLbl, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, confPct);
     confRow.setAlignment(Pos.CENTER_LEFT);

     ProgressBar pb = new ProgressBar(0);
     pb.setMaxWidth(Double.MAX_VALUE);
     pb.setPrefHeight(6);
     pb.setStyle("-fx-accent:" + CustomColours.TEAL.getValue() + ";-fx-background-color:#0d0d0d;");
     new Timeline(
         new KeyFrame(Duration.ZERO,       new KeyValue(pb.progressProperty(), 0)),
         new KeyFrame(Duration.millis(900), new KeyValue(pb.progressProperty(), conf, Interpolator.EASE_OUT))
     ).play();

     // Status chips.
     HBox stats = new HBox(12,
         chip("Objects", String.valueOf(preds.size())),
         chip("Total Nodes",  String.valueOf(nodes)),
         chip("Total Edges",  String.valueOf(edges))
     );

     panel.getChildren().addAll(navLbl, confRow, pb, stats);
 }

 private VBox buildColorIndicator() {
     VBox indicator = new VBox(12);
     indicator.setPadding(new Insets(18));
     indicator.setStyle(UIStyles.smallCard());
     
     Label head = sectionHead("CATEGORY COLORS");
     indicator.getChildren().add(head);
     
     indicator.getChildren().add(colorRow("#008080", "Tactile Indicator"));
     indicator.getChildren().add(colorRow("#2dce89", "Horizontal Path"));
     indicator.getChildren().add(colorRow("#5e72e4", "Vertical Path"));
     indicator.getChildren().add(colorRow("#f5a623", "Warning Tactile"));
     indicator.getChildren().add(colorRow("#FFFFFF", "Unknown"));
     
     return indicator;
 }

 private HBox colorRow(String color, String label) {
     Rectangle r = new Rectangle(14, 14, Color.web(color));
     r.setArcWidth(4); r.setArcHeight(4);
     Label l = new Label(label);
     l.setFont(Font.font("Courier New", 11));
     l.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     HBox row = new HBox(10, r, l);
     row.setAlignment(Pos.CENTER_LEFT);
     return row;
 }


 
//=========================
 // SIMILARITY SCREEN
 // =========================

 /**
  * Displays the Similarity Detection screen.
  * Allows user to select two images and compare graph structures.
  */
 private void showSimilarity() {
     simFileA = null; simFileB = null; simImageA = null; simImageB = null;

     //Header.
     Label title = screenTitle("  Similarity Detection");
     Label desc = grayLabel(
         "Select two dataset images. The system builds a graph from each using " +
         "Sobel edge detection, then computes a structural similarity score by " +
         "comparing edge topology between the two AdjacencyListGraphs."
     );
     desc.setFont(Font.font("Courier New",FontWeight.BOLD, 11));
     VBox header = new VBox(10, title, hLine(), desc);
     header.setPadding(new Insets(30, 40, 0, 40));

     //Image slot holders.
     StackPane holderA = imageSlot("Image A");
     StackPane holderB = imageSlot("Image B");
     Label lblA = slotLabel("No image selected");
     lblA.setFont(Font.font("Courier New",FontWeight.BOLD, 11));
     Label lblB = slotLabel("No image selected");
     lblB.setFont(Font.font("Courier New",FontWeight.BOLD, 11));

     //Result panel.
     VBox simResult = new VBox(16);
     simResult.setVisible(false);

     FileChooser fc = new FileChooser();
     fc.setInitialDirectory(new File("src/Dataset"));
     fc.getExtensionFilters().add(
         new FileChooser.ExtensionFilter("Images","*.png","*.jpg","*.jpeg","*.bmp"));
     // SELECT IMAGE A (CustomButton - Blue)
     CustomButton btnA = new CustomButton("  Select Image 1", CustomColours.BUTTON_BLUE);
     btnA.setMaxWidth(Double.MAX_VALUE);
     btnA.setOnAction(e -> {
         File f = fc.showOpenDialog(null);
         if (f == null) return;
         simFileA = f; simImageA = new Image(f.toURI().toString());
         simEntryA = knnDetector.findImageByName(f.getName());
         
         List<String> labels = (simEntryA != null) ? generateBoxLetters(simEntryA.boxes.size()) : null;
         holderA.getChildren().setAll(buildImageDisplay(simImageA, f, 390, 280, labels));
         
         lblA.setText("  " + f.getName());
         simResult.setVisible(false);
         if (simEntryA == null) log("[SIM] Warn: Image A not found in KNN JSON data.");
         log("[SIM] Image A: " + f.getName());
     });

     // SELECT IMAGE B (CustomButton - Blue)
     CustomButton btnB = new CustomButton("  Select Image 2", CustomColours.BUTTON_BLUE);
     btnB.setMaxWidth(Double.MAX_VALUE);
     btnB.setOnAction(e -> {
         File f = fc.showOpenDialog(null);
         if (f == null) return;
         simFileB = f; simImageB = new Image(f.toURI().toString());
         simEntryB = knnDetector.findImageByName(f.getName());
         
         List<String> labels = (simEntryB != null) ? generateBoxLetters(simEntryB.boxes.size()) : null;
         holderB.getChildren().setAll(buildImageDisplay(simImageB, f, 390, 280, labels));
         
         lblB.setText("  " + f.getName());
         simResult.setVisible(false);
         if (simEntryB == null) log("[SIM] Warn: Image B not found in KNN JSON data.");
         log("[SIM] Image B: " + f.getName());
     });

     //Image columns.
     VBox colA = new VBox(8, holderA, btnA, lblA);
     colA.setAlignment(Pos.TOP_CENTER);
     HBox.setHgrow(colA, Priority.ALWAYS);

     //VS divide.
     Label vs = new Label("VS");
     vs.setFont(Font.font("Courier New", FontWeight.BOLD, 20));
     vs.setTextFill(Color.web(CustomColours.TEAL.getValue()));
     vs.setMinWidth(44);
     vs.setAlignment(Pos.CENTER);
     vs.setEffect(new DropShadow(10, Color.web(CustomColours.TEAL.getValue(), 0.5)));

     VBox colB = new VBox(8, holderB, btnB, lblB);
     colB.setAlignment(Pos.TOP_CENTER);
     HBox.setHgrow(colB, Priority.ALWAYS);

     HBox imageRow = new HBox(14, colA, vs, colB);
     imageRow.setAlignment(Pos.TOP_CENTER);

     // COMPARE button (CustomButton - Green)
     CustomButton btnCompare = new CustomButton("  Compare Images", CustomColours.BUTTON_GREEN);
     btnCompare.setMaxWidth(300);
     btnCompare.setOnAction(e -> runSimilarity(simResult, btnCompare));

     HBox compareRow = new HBox(btnCompare);
     compareRow.setAlignment(Pos.CENTER);

     VBox screen = new VBox(20,
         header,
         padH(imageRow, 40),
         padH(compareRow, 40),
         padH(simResult, 40)
     );
     screen.setPadding(new Insets(0, 0, 30, 0));
     screen.setStyle(UIStyles.mainBackground());

     ScrollPane sp = scrollWrap(screen);
     contentArea.getChildren().setAll(sp);
     fadeIn(sp);
 }

 /** Runs Extraction on both images, then shows result */
 private void runSimilarity(VBox panel, CustomButton btn) {
     if (simEntryA == null || simEntryB == null) {
         log("[SIM] Please select both Image A and Image B");
         return;
     }

     btn.setDisable(true);
     log("[SIM] Comparing graph structures...");

     new Thread(() -> {
         List<KnnSimilarityDetector.MatchResult> results = knnDetector.compareImages(simEntryA, simEntryB);
         
         double bestSim = results.isEmpty() ? 0 : results.get(0).similarity;
         int matches = 0;
         for (var r : results) if (r.catA == r.catB) matches++;

         final double fSim = bestSim;
         final int fMatches = matches;
         final int fTotal = results.size();

         Platform.runLater(() -> {
             fillSimilarityResult(panel, results, fSim, fMatches, fTotal);
             panel.setVisible(true);
             btn.setDisable(false);
             log(String.format("[SIM] Comparison complete. Best Match: %.2f%%", fSim));
         });
     }).start();
 }

 /** Fills the similarity result panel */
 private void fillSimilarityResult(VBox panel, List<KnnSimilarityDetector.MatchResult> results, double bestSim, int matches, int total) {
     panel.getChildren().clear();
     panel.setStyle(UIStyles.imageHolder());
     panel.setPadding(new Insets(22));

     Label head = sectionHead("KNN STRUCTURAL SIMILARITY RESULTS");
     panel.getChildren().add(head);

     // Summary row
     HBox summary = new HBox(15,
         chip("Best Match", String.format("%.1f%%", bestSim)),
         chip("Matches", matches + "/" + total)
     );
     panel.getChildren().add(summary);

     panel.getChildren().add(hLine());

     // TableView for results
     TableView<SimilarityRow> table = new TableView<>();
     table.setPrefHeight(380);
     table.setStyle(UIStyles.tableView());

     TableColumn<SimilarityRow, String> colRank = new TableColumn<>("Rank");
     colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
     colRank.setPrefWidth(60);

     TableColumn<SimilarityRow, String> colBoxB = new TableColumn<>("Image 2");
     colBoxB.setCellValueFactory(new PropertyValueFactory<>("boxB"));
     colBoxB.setPrefWidth(70);

     TableColumn<SimilarityRow, String> colBoxA = new TableColumn<>("Image 1");
     colBoxA.setCellValueFactory(new PropertyValueFactory<>("boxA"));
     colBoxA.setPrefWidth(70);

     TableColumn<SimilarityRow, String> colCatB = new TableColumn<>("Category 2");
     colCatB.setCellValueFactory(new PropertyValueFactory<>("catB"));
     colCatB.setPrefWidth(80);

     TableColumn<SimilarityRow, String> colCatA = new TableColumn<>("Category 1");
     colCatA.setCellValueFactory(new PropertyValueFactory<>("catA"));
     colCatA.setPrefWidth(80);

     TableColumn<SimilarityRow, String> colSim = new TableColumn<>("Similarity %");
     colSim.setCellValueFactory(new PropertyValueFactory<>("similarity"));
     colSim.setPrefWidth(90);

     TableColumn<SimilarityRow, String> colMatch = new TableColumn<>("Match");
     colMatch.setCellValueFactory(new PropertyValueFactory<>("match"));
     colMatch.setPrefWidth(70);

     table.getColumns().addAll(colRank, colBoxB, colBoxA, colCatB, colCatA, colSim, colMatch);

     for (int i = 0; i < results.size(); i++) {
         KnnSimilarityDetector.MatchResult r = results.get(i);
         table.getItems().add(new SimilarityRow(
             i + 1,
             String.valueOf((char)('A' + (r.indexB % 26))),
             String.valueOf((char)('A' + (r.indexA % 26))),
             KnnSimilarityDetector.CAT_NAMES[r.catB % 4],
             KnnSimilarityDetector.CAT_NAMES[r.catA % 4],
             r.similarity,
             r.catA == r.catB
         ));
     }

     panel.getChildren().add(table);
 }

 /** Draws the circular arc gauge for the similarity score (deprecated/stub) */
 private void drawGauge(Canvas c, double sim) { }

 private String simDesc(double sim) {
     if (sim > 90) return "Identical Structure";
     if (sim > 70) return "Highly Similar";
     if (sim > 40) return "Moderate Similarity";
     return "Distinct Structure";
 }

 // =======================
 // IMAGE DISPLAY HELPERS
 // =======================

 /** Builds a stacked View and visualize canvas showing the image with bbox overlay */
 private StackPane buildImageDisplay(Image img, File f, double maxW, double maxH) {
     return buildImageDisplay(img, f, maxW, maxH, null);
 }

 private StackPane buildImageDisplay(Image img, File f, double maxW, double maxH, List<String> labels) {
     double scaleW = maxW / img.getWidth();
     double scaleH = maxH / img.getHeight();
     double scale  = Math.min(scaleW, scaleH);
     double dW = img.getWidth() * scale;
     double dH = img.getHeight() * scale;

     View imgCanvas = new View((int)dW, (int)dH);
     imgCanvas.getGraphicsContext2D().drawImage(img, 0, 0, dW, dH);

     visualise overlay = new visualise((int)dW, dH, scale);
     List<annotations> anns = fetchAnnotationsFor(f);
     drawAnnotations(overlay, anns, scale, labels);

     StackPane sp = new StackPane(imgCanvas, overlay);
     sp.setMaxWidth(dW); sp.setMaxHeight(dH);
     return sp;
 }

 private void drawAnnotations(visualise v, List<annotations> anns, double scale, List<String> labels) {
     v.drawLabeledRectangles(anns, labels);
 }

 private List<String> generateBoxLetters(int count) {
     List<String> labels = new ArrayList<>();
     for (int i = 0; i < count; i++) {
         labels.add(String.valueOf((char)('A' + (i % 26))));
     }
     return labels;
 }

 private void drawGraphOverlay(StackPane imageHolder, Extraction ext, List<annotations> anns) {
     // Find the visualise canvas in the stackpane
     visualise v = null;
     for (var node : imageHolder.getChildren()) {
         if (node instanceof visualise) {
             v = (visualise) node;
             break;
         } else if (node instanceof StackPane) {
             for (var subNode : ((StackPane) node).getChildren()) {
                 if (subNode instanceof visualise) {
                     v = (visualise) subNode;
                     break;
                 }
             }
         }
         if (v != null) break;
     }

     if (v != null) {
         v.drawGraph(ext.graphs, anns);
     }
 }

 

 // =========================
 // CLASSIFICATION LOGIC
 // =========================

 /**
  * Derives a navigation command based on graph node and edge counts.
  *
  * @param nodes number of graph nodes
  * @param edges number of graph edges
  * @return navigation command
  */
 private NavCommand deriveNavCommand(int nodes, int edges) {
     if (nodes < 5) return NavCommand.LOST;
     if (edges < 3) return NavCommand.STOP;
     double r = (double) edges / Math.max(nodes,1);
     if (r > 1.8) return NavCommand.FORWARD;
     if (r > 1.2) return NavCommand.RIGHT;
     return NavCommand.LEFT;
 }

 private String labelFromCommand(NavCommand cmd) {
     return switch (cmd) {
         case FORWARD -> "STRAIGHT PATH";
         case LEFT    -> "LEFT CORNER";
         case RIGHT   -> "RIGHT CORNER";
         case STOP    -> "OBSTACLE / STOP";
         case LOST    -> "PATH LOST";
     };
 }

 /**
  * Maps navigation command to a color string.
  *
  * @param cmd navigation command
  * @return hex color string
  */
 private String colourFromCommand(NavCommand cmd) {
     return switch (cmd) {
         case FORWARD     -> "#2dce89";
         case LEFT, RIGHT -> CustomColours.TEAL.getValue();
         case STOP        -> "#f5a623";
         case LOST        -> "#f5365c";
     };
 }

 /**
  * Computes heuristic confidence score based on graph statistics.
  *
  * @param nodes number of nodes.
  * @param edges number of edges.
  * @return confidence score between 0 and 1.
  */
 private double heuristicConfidence(int nodes, int edges) {
     if (nodes == 0) return 0.0;
     return 0.55 + Math.min(1.0, edges / (nodes * 2.0)) * 0.42;
 }


  /**
  * Fetches annotations for a given image file by searching across all split databases.
  *
  * @param f image file
  * @return list of annotations
  */
 private List<annotations> fetchAnnotationsFor(File f) {
     if (f == null) return List.of();
     
     // First try current loader
     List<annotations> anns = tryFetch(currentLoader, f);
     if (!anns.isEmpty()) return anns;

     // Try others
     for (String dbPath : new String[]{DB_TRAIN, DB_VALID, DB_TEST}) {
         dbLoader tempLoader = new dbLoader(dbPath);
         anns = tryFetch(tempLoader, f);
         if (!anns.isEmpty()) {
             log("[DB] Found " + f.getName() + " in " + dbPath);
             return anns;
         }
     }
     
     log("[ERR] No annotations found for " + f.getName() + " in any database.");
     return List.of();
 }

 private List<annotations> tryFetch(dbLoader loader, File f) {
     try {
         for (database.models.Images img : loader.getAllImages()) {
             if (img.Filename().equalsIgnoreCase(f.getName()))
                 return loader.getAnnotationsForImage(img.ID());
         }
     } catch (Exception ex) { /* silent */ }
     return List.of();
 }



//=========================
 // LOGGING
 // =========================

 /**
  * Builds the log area at the bottom of the UI.
  */
 private void buildLog() {
     logArea = new TextArea();
     logArea.setEditable(false);
     logArea.setPrefHeight(110);
     logArea.setWrapText(true);
     logArea.setStyle(UIStyles.logArea());
 }

 /**
  * Appends a message to the log area.
  *
  * @param msg message to log
  */
 private void log(String msg) {
     Platform.runLater(() -> { if (logArea != null) logArea.appendText(msg + "\n"); });
 }


 // =========================
 // HELPER LABELS
 // =========================
 private Label screenTitle(String text) {
     Label l = new Label(text);
     l.setFont(Font.font("Courier New", FontWeight.BOLD, 22));
     l.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     return l;
 }

 private Label grayLabel(String text) {
     Label l = new Label(text);
     l.setFont(Font.font("Courier New", 12));
     l.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     l.setWrapText(true);
     l.setLineSpacing(3);
     return l;
 }

 private Label sectionHead(String text) {
     Label l = new Label(text);
     l.setFont(Font.font("Courier New", FontWeight.BOLD, 9));
     l.setTextFill(Color.web(CustomColours.TEAL.getValue()));
     return l;
 }

 private Label slotLabel(String text) {
     Label l = new Label(text);
     l.setFont(Font.font("Courier New", 10));
     l.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     return l;
 }

 
//=========================
 // UTILITY COMPONENTS
 // =========================

 /**
  * Creates a horizontal divider line.
  *
  * @return Region styled as divider.
  */
 private Region hLine() {
     Region r = new Region();
     r.setPrefHeight(1);
     r.setStyle(UIStyles.divider());
     return r;
 }


 /**
  * Wraps a node with horizontal padding.
  *
  * @param node Node to wrap
  * @param pad padding value
  * @return VBox with padding
  */
 private HBox padH(Node n, double pad) {
     HBox box = new HBox(n);
     box.setPadding(new Insets(0, pad, 0, pad));
     HBox.setHgrow(n, Priority.ALWAYS);
     if (n instanceof Region) ((Region)n).setMaxWidth(Double.MAX_VALUE);
     return box;
 }

 private VBox emptySlotInner(String hint) {
     Label icon = new Label("+"); icon.setFont(Font.font("Courier New", FontWeight.BOLD, 42));
     icon.setTextFill(Color.web(CustomColours.BORDER.getValue()));
     Label l = new Label(hint); l.setFont(Font.font("Courier New", 13));
     l.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     VBox b = new VBox(10, icon, l); b.setAlignment(Pos.CENTER);
     return b;
 }

 private StackPane imageSlot(String lbl) {
     Label icon = new Label("+"); icon.setFont(Font.font("Courier New", FontWeight.BOLD, 30));
     icon.setTextFill(Color.web(CustomColours.BORDER.getValue()));
     Label l = new Label(lbl); l.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
     l.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     VBox inner = new VBox(8, icon, l); inner.setAlignment(Pos.CENTER);
     StackPane sp = new StackPane(inner);
     sp.setMinSize(390, 275); sp.setMaxSize(390, 275);
     sp.setStyle(UIStyles.smallCard());
     return sp;
 }

 private VBox chip(String key, String val) {
     Label k = new Label(key); k.setFont(Font.font("Courier New",9)); k.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     Label v = new Label(val); v.setFont(Font.font("Courier New", FontWeight.BOLD,15)); v.setTextFill(Color.web(CustomColours.WHITE.getValue()));
     VBox c = new VBox(2,k,v); c.setAlignment(Pos.CENTER);
     c.setPadding(new Insets(10,18,10,18));
     c.setStyle(UIStyles.chip());
     return c;
 }

 /**
  * Wraps a node inside a ScrollPane with consistent styling.
  *
  * @param node Node to wrap
  * @return ScrollPane containing the node
  */
 private ScrollPane scrollWrap(Node content) {
     ScrollPane sp = new ScrollPane(content);
     sp.setFitToWidth(true);
     sp.setStyle(UIStyles.scrollPane());
     return sp;
 }

 /**
  * Applies fade-in animation to a node.
  *
  * @param node Node to animate.
  */
 private void fadeIn(Node n) {
     FadeTransition ft = new FadeTransition(Duration.millis(400), n);
     ft.setFromValue(0); ft.setToValue(1); ft.play();
 }

 public static void main(String[] args) { launch(args); }
 }