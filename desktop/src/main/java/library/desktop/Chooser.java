package library.desktop;

import java.util.List;
import java.util.function.Function;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

/**
 * A combo box that shows each item by the text a function makes of it. The rows that
 * come from the table presenter are strings already; what a list shows of a row, the
 * name alone or the number and the book, is the dialog's to choose, and this is
 * where it chooses. Part of the view.
 */
class Chooser<T> extends ComboBox<T> {

    Chooser(List<T> items, Function<T, String> text) {
        super(FXCollections.observableArrayList(items));
        setConverter(new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : text.apply(item);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        });
    }
}
