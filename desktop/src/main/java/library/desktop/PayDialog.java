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
import library.application.listmembers.ListMembersInput;
import library.application.payfine.PayFineInput;
import library.application.payfine.PayFineRequest;
import library.presenters.MemberRow;
import library.presenters.TablePresenter;
import library.presenters.TextPresenter;

public class PayDialog extends Stage {

    public String result = "";

    public PayDialog(PayFineInput payFine, ListMembersInput listMembers,
                     TablePresenter tables, TextPresenter sentences) {
        setTitle("Pay fines");
        initModality(Modality.APPLICATION_MODAL);

        listMembers.execute(tables);
        Chooser<MemberRow> memberBox = new Chooser<>(tables.members(), MemberRow::name);
        Label message = new Label();
        message.setStyle("-fx-text-fill: red;");

        Button pay = new Button("Pay");
        pay.setOnAction(e -> {
            MemberRow member = memberBox.getValue();
            if (member == null) {
                message.setText("Choose a member.");
                return;
            }
            try {
                payFine.execute(new PayFineRequest(member.name()), sentences);
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
        grid.add(message, 0, 1, 2, 1);
        grid.add(new HBox(10, pay, cancel), 0, 2, 2, 1);

        setScene(new Scene(grid));
    }
}
