package library.desktop;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import library.application.Refusal;
import library.application.listcopies.ListCopiesInput;
import library.application.returncopy.ReturnCopyInput;
import library.application.returncopy.ReturnCopyRequest;
import library.presenters.CopyRow;
import library.presenters.TablePresenter;
import library.presenters.TextPresenter;

public class ReturnDialog extends Stage {

    public String result = "";

    public ReturnDialog(ReturnCopyInput returnCopy, ListCopiesInput listCopies,
                        TablePresenter tables, TextPresenter sentences) {
        setTitle("Return a copy");
        initModality(Modality.APPLICATION_MODAL);

        listCopies.execute(tables);
        Chooser<CopyRow> loanBox = new Chooser<>(tables.loans(), Loans::describe);
        loanBox.setPrefWidth(320);
        Label message = new Label();
        message.setStyle("-fx-text-fill: red;");

        Button returnButton = new Button("Return");
        returnButton.setOnAction(e -> {
            CopyRow copy = loanBox.getValue();
            if (copy == null) {
                message.setText("Choose a loan.");
                return;
            }
            try {
                returnCopy.execute(new ReturnCopyRequest(copy.number()), sentences);
                result = sentences.text();
                close();
            } catch (Refusal ex) {
                message.setText(ex.getMessage());
            }
        });

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> close());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Loan"), 0, 0);
        grid.add(loanBox, 1, 0);
        grid.add(message, 0, 1, 2, 1);
        grid.add(new HBox(10, returnButton, cancel), 0, 2, 2, 1);

        setScene(new Scene(grid));
    }
}
