package ph.kana.b64image.dialog;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.util.function.Consumer;

public class DndInitializer {

	private static DndInitializer instance;

	public static DndInitializer withFileAction(Consumer<File> afterDragAction) {
		if (instance == null) {
			instance = new DndInitializer(afterDragAction);
		}
		return instance;
	}

	private final Consumer<File> afterDragAction;

	public DndInitializer(Consumer<File> afterDragAction) {
		this.afterDragAction = afterDragAction;
	}

	public void dragOverEvent(DragEvent dragEvent) {
		boolean dragFromOutside = (dragEvent.getGestureSource() == null);
		boolean draggingFilesFromOutside = dragFromOutside &&
			dragEvent
				.getDragboard()
				.hasFiles();
		if (draggingFilesFromOutside) {
			dragEvent.acceptTransferModes(TransferMode.ANY);
		}

		dragEvent.consume();
	}

	public void dragDroppedEvent(DragEvent dragEvent) {
		Dragboard dragboard = dragEvent
			.getDragboard();
		if (dragboard.hasFiles()) {
			dragboard
				.getFiles()
				.stream()
				.findFirst()
				.ifPresent(afterDragAction);
		}
		dragEvent.setDropCompleted(true);
		dragEvent.consume();
	}
}
