package library.domain;

import java.util.ArrayList;
import java.util.List;

import library.model.Book;
import library.model.Copy;

class FakeCopyRepository implements CopyRepository {

    private final List<Copy> copies = new ArrayList<>();
    final List<Copy> saved = new ArrayList<>();

    FakeCopyRepository(Copy... copies) {
        this.copies.addAll(List.of(copies));
    }

    @Override
    public List<Copy> all() {
        return List.copyOf(copies);
    }

    @Override
    public List<Copy> ofBook(Book book) {
        List<Copy> result = new ArrayList<>();
        for (Copy copy : copies) {
            if (copy.book() == book) {
                result.add(copy);
            }
        }
        return result;
    }

    @Override
    public Copy numbered(String number) {
        for (Copy copy : copies) {
            if (copy.number().equals(number)) {
                return copy;
            }
        }
        return null;
    }

    @Override
    public void save(Copy copy) {
        saved.add(copy);
    }
}
