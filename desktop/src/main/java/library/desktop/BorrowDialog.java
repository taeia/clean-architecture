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
import library.application.borrowcopy.BorrowCopyInput;
import library.application.borrowcopy.BorrowCopyRequest;
import library.application.listcopies.ListCopiesInput;
import library.application.listmembers.ListMembersInput;
import library.presenters.CopyRow;
import library.presenters.MemberRow;
import library.presenters.TablePresenter;
import library.presenters.TextPresenter;

/**
 * The Borrow button's dialog. A view and a controller in one class: it fills its two
 * lists from the rows the table presenter made of two listings, builds a request from
 * what was chosen, calls the use case, and keeps the sentence the text presenter made
 * of the response for the status bar. It decides nothing about the words.
 */
public class BorrowDialog extends Stage {

    public String result = "";

    public BorrowDialog(BorrowCopyInput borrowCopy, ListMembersInput listMembers, ListCopiesInput listCopies,
                        TablePresenter tables, TextPresenter sentences) {
        setTitle("Borrow a copy");
        initModality(Modality.APPLICATION_MODAL);

        listMembers.execute(tables);
        listCopies.execute(tables);
        Chooser<MemberRow> memberBox = new Chooser<>(tables.members(), MemberRow::name);
        Chooser<CopyRow> copyBox = new Chooser<>(tables.copies(), c -> c.number() + " (" + c.book() + ")");
        Label message = new Label();
        message.setStyle("-fx-text-fill: red;");

        Button borrow = new Button("Borrow");
        borrow.setOnAction(e -> {
            MemberRow member = memberBox.getValue();
            CopyRow copy = copyBox.getValue();
            if (member == null || copy == null) {
                message.setText("Choose a member and a copy.");
                return;
            }
            try {
                borrowCopy.execute(new BorrowCopyRequest(member.name(), copy.number()), sentences);
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
        grid.add(new Label("Member"), 0, 0);
        grid.add(memberBox, 1, 0);
        grid.add(new Label("Copy"), 0, 1);
        grid.add(copyBox, 1, 1);
        grid.add(message, 0, 2, 2, 1);
        grid.add(new HBox(10, borrow, cancel), 0, 3, 2, 1);

        setScene(new Scene(grid));
    }
}
