package com.github.thejunkjon.junkirc.ui;

import com.github.thejunkjon.junkirc.model.ServerConfiguration;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.github.thejunkjon.junkirc.utils.ResourceUtils.getResource;
import static com.github.thejunkjon.junkirc.utils.ResourceUtils.getResourceAsStream;

final class ConnectDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectDialog.class);
    private static final String WINDOW_TITLE = "Connect To IRC Server";
    private static final String FXML_CONNECT_DIALOG = "fxml/dialog/ConnectDialog.fxml";
    private static final String CONFIG_DEFAULT_IRC_SERVERS_JSON = "config/defaultIrcServers.json";
    private final FXMLLoader fxmlLoader = new FXMLLoader(getResource(FXML_CONNECT_DIALOG));
    private final Stage stage;

    ConnectDialog(final Stage stage) {
        this.stage = stage;
    }

    void show() throws IOException {
        final Parent root = fxmlLoader.load();
        stage.setTitle(WINDOW_TITLE);

        final Scene scene = new Scene(root);
        buildIrcServersList(scene);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void buildIrcServersList(final Scene scene) {
        final ListView<String> listView = (ListView<String>) scene.lookup("#ircServersList");
        final List<ServerConfiguration> configurationsForIrcServers = getConfigurationsForIrcServers();
        final ObservableList<String> observableList = FXCollections.observableArrayList();
        for (final ServerConfiguration serverConfiguration : configurationsForIrcServers) {
            observableList.add(serverConfiguration.name);
        }

        listView.setItems(observableList);
        listView.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    public void changed(final ObservableValue<? extends String> observableValue,
                                        final String oldValue,
                                        final String newValue) {
                        final TextField host = (TextField) scene.lookup("#host");
                        final TextField port = (TextField) scene.lookup("#port");
                        final ServerConfiguration serverConfiguration =
                                getMatchingIrcServerConfigurationFor(newValue);
                        host.setText(serverConfiguration.host);
                        port.setText(String.valueOf(serverConfiguration.port));
                    }

                    private ServerConfiguration getMatchingIrcServerConfigurationFor(String newValue) {
                        for (ServerConfiguration serverConfiguration : configurationsForIrcServers) {
                            if (serverConfiguration.name.equals(newValue)) {
                                return serverConfiguration;
                            }
                        }
                        return new ServerConfiguration();
                    }
                });
    }

    public List<ServerConfiguration> getConfigurationsForIrcServers() {
        final List<ServerConfiguration> configurationsForIrcServers = new ArrayList<>();
        try (final InputStream defaultIrcServerJsonFileStream =
                     getResourceAsStream(CONFIG_DEFAULT_IRC_SERVERS_JSON)) {
            final Type listType = new TypeToken<ArrayList<ServerConfiguration>>() {
            }.getType();
            configurationsForIrcServers.addAll(
                    new Gson().fromJson(new InputStreamReader(defaultIrcServerJsonFileStream, "UTF-8"), listType));
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return configurationsForIrcServers;
    }
}
