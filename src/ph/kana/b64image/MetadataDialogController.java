package ph.kana.b64image;


import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import ph.kana.b64image.file.FileMetadata;

import java.util.List;

public class MetadataDialogController {

	private List<FileMetadata> metadata;

	@FXML private Pane rootPane;

	@FXML private TableView<FileMetadata> infoTable;

	@FXML
	private void closeWindow() {
		Stage window = (Stage) getWindow();
		window.close();
	}

	public void setMetadata(List<FileMetadata> metadata) {
		this.metadata = metadata;
		populateTable();
	}

	private Window getWindow() {
		return rootPane
			.getScene()
			.getWindow();
	}

	private void populateTable() {
		List<TableColumn<FileMetadata, ?>> columns = infoTable.getColumns();
		TableColumn<FileMetadata, ?> keyColumn = columns.get(0);
		keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));

		TableColumn<FileMetadata, ?> valueColumn = columns.get(1);
		valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

		List<FileMetadata> tableData = infoTable.getItems();
		tableData.addAll(metadata);
	}
}
