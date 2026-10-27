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
import library.application.listbooks.ListBooksInput;
import library.application.listmembers.ListMembersInput;
import library.application.reservebook.ReserveBookInput;
import library.application.reservebook.ReserveBookRequest;
import library.presenters.BookRow;
import library.presenters.MemberRow;
import library.presenters.TablePresenter;
import library.presenters.TextPresenter;

public class ReserveDialog extends Stage {

    public String result = "";

    public ReserveDialog(ReserveBookInput reserveBook, ListMembersInput listMembers, ListBooksInput listBooks,
                         TablePresenter tables, TextPresenter sentences) {
        setTitle("Reserve a book");
        initModality(Modality.APPLICATION_MODAL);

        listMembers.execute(tables);
        listBooks.execute(tables);
        Chooser<MemberRow> memberBox = new Chooser<>(tables.members(), MemberRow::name);
        Chooser<BookRow> bookBox = new Chooser<>(tables.books(), BookRow::title);
        Label message = new Label();
        message.setStyle("-fx-text-fill: red;");

        Button reserve = new Button("Reserve");
        reserve.setOnAction(e -> {
            MemberRow member = memberBox.getValue();
            BookRow book = bookBox.getValue();
            if (member == null || book == null) {
                message.setText("Choose a member and a book.");
                return;
            }
            try {
                reserveBook.execute(new ReserveBookRequest(member.name(), book.title()), sentences);
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
        grid.add(new Label("Book"), 0, 1);
        grid.add(bookBox, 1, 1);
        grid.add(message, 0, 2, 2, 1);
        grid.add(new HBox(10, reserve, cancel), 0, 3, 2, 1);

        setScene(new Scene(grid));
    }
}
