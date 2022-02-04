package ph.kana.b64image;


import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ph.kana.b64image.file.FileMetadata;

import java.util.List;

public class MetadataDialogController extends AbstractController {

	private List<FileMetadata> metadata;

	@FXML private TableView<FileMetadata> infoTable;

	@FXML
	private void closeWindow() {
		Stage window = getWindow();
		window.close();
	}

	public void setMetadata(List<FileMetadata> metadata) {
		this.metadata = metadata;
		populateTable();
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
