import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

import javafx.concurrent.Worker;
import java.util.ArrayList;

class c2w_BrowserWindow extends Stage {
private c2w_WebBrowser c2w_owner;
private WebEngine c2w_webEngine;
private Menu c2w_windowMenu;

c2w_BrowserWindow(c2w_WebBrowser c2w_browser, String c2w_initialURL) {
c2w_owner = c2w_browser;

WebView webview = new WebView();
c2w_webEngine = webview.getEngine();

Label status = new Label("Status: Idle");

status.setMaxWidth(Double.POSITIVE_INFINITY);
Label location = new Label("Location: (empty)");

location.setMaxWidth(Double.POSITIVE_INFINITY);
TextField urlInput = new TextField();

urlInput.setMaxWidth(600);
Button loadButton = new Button("Load");

loadButton.setOnAction( e -> doLoad(urlInput.getText()) );
loadButton.defaultButtonProperty().bind(

urlInput.focusedProperty() );

Button cancelButton = new Button("Cancel");

cancelButton.setDisable(true);

HBox loader = new HBox(8,new Label("URL:"), urlInput, loadButton,

cancelButton);

HBox.setHgrow(urlInput, Priority.ALWAYS);
VBox bottom = new VBox(10, location, status, loader);
bottom.setStyle("-fx-padding: 10px; -fx-border-color:black;

-fx-border-width:3px 0 0 0");

BorderPane root = new BorderPane(webview);
root.setBottom(bottom);
root.setTop(makeMenuBar());
setScene( new Scene(root) );

c2w_webEngine.locationProperty().addListener( (o,oldVal,newVal) ->
{

if (newVal == null || newVal.equals("about:blank"))
location.setText("Location: (empty)");
else
location.setText("Location: " + newVal);
});
c2w_webEngine.titleProperty().addListener( (o,oldVal,newVal) -> {
if (newVal == null)

setTitle("Untitled " +
c2w_owner.c2w_getNextUntitledCount());

else
setTitle(newVal);
});

c2w_webEngine.getLoadWorker().stateProperty().addListener(
(o,oldVal,newVal) -> {

status.setText("Status: " + newVal);
switch (newVal) {
case READY:
status.setText("Status: Idle.");
break;
case SCHEDULED:
case RUNNING:
status.setText("Status: Loading a web page.");
break;
case SUCCEEDED:
status.setText("Status: Web page has been successfully

loaded.");

break;
case FAILED:
status.setText("Status: Loading of the web page has

failed.");

break;
case CANCELLED:
status.setText("Status: Loading of the web page has been

cancelled.");

break;
}
cancelButton.setDisable(newVal != Worker.State.RUNNING);
});
cancelButton.setOnAction( e -> {
if ( c2w_webEngine.getLoadWorker().getState() ==

Worker.State.RUNNING )

c2w_webEngine.getLoadWorker().cancel();
});

c2w_webEngine.setOnAlert( evt ->

SimpleDialogs.message(evt.getData(), "Alert from web page") );

c2w_webEngine.setPromptHandler( promptData ->

SimpleDialogs.prompt(promptData.getMessage(),
"Query from web page",

promptData.getDefaultValue()));

c2w_webEngine.setConfirmHandler( str ->

SimpleDialogs.confirm(str, "Confirmation

Needed").equals("yes") );

if (c2w_initialURL != null) {

doLoad(c2w_initialURL);
}
} 
private void doLoad(String url) {
if (url == null || url.trim().length() == 0)
return;
url = url.trim();
if ( ! url.matches("^[a-zA-Z]+:.*")) {
url = "http://" + url;
}
System.out.println("Loading URL " + url);
c2w_webEngine.load(url);
}

private MenuBar makeMenuBar() {
MenuItem newWin = new MenuItem("New Window");
newWin.setOnAction( e -> c2w_owner.c2w_newBrowserWindow(null) );
MenuItem close = new MenuItem("Close Window");
close.setOnAction( e -> hide() );
MenuItem open = new MenuItem("Open URL in New Window...");
open.setOnAction( e -> {
String url = SimpleDialogs.prompt(

"Enter the URL of the page that you want to open.",

"Get URL");

if (url != null && url.trim().length() > 0)
c2w_owner.c2w_newBrowserWindow(url);
});
c2w_windowMenu = new Menu("Window");
c2w_windowMenu.getItems().addAll(newWin,close,open,new

SeparatorMenuItem());

c2w_windowMenu.setOnShowing( e -> populateWindowMenu() );
MenuBar menubar = new MenuBar(c2w_windowMenu);
return menubar;
}

private void populateWindowMenu() {
ArrayList<c2w_BrowserWindow> windows =

c2w_owner.getOpenWindowList();

while (c2w_windowMenu.getItems().size() > 4) {

c2w_windowMenu.getItems().remove(c2w_windowMenu.getItems().size() - 1);

}
if (windows.size() > 1) {

MenuItem item = new MenuItem("Close All and Exit");
item.setOnAction( e -> Platform.exit() );
c2w_windowMenu.getItems().add(item);
c2w_windowMenu.getItems().add( new SeparatorMenuItem() );
}
for (c2w_BrowserWindow window : windows) {
String title = window.getTitle();

if (title.length() > 60) {

title = title.substring(0,57) + ". . .";
}
MenuItem item = new MenuItem(title);
final c2w_BrowserWindow win = window;

item.setOnAction( e -> win.requestFocus() );
c2w_windowMenu.getItems().add(item);
if (window == this) {

item.setDisable(true);
}
}
}

}