package library.inmemory;

import java.util.ArrayList;
import java.util.List;

import library.model.Book;
import library.model.Copy;
import library.domain.CopyRepository;

public class InMemoryCopyRepository implements CopyRepository {

    private final List<Copy> copies = new ArrayList<>();

    @Override
    public List<Copy> all() {
        return new ArrayList<>(copies);
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
        if (!copies.contains(copy)) {
            copies.add(copy);
        }
    }

    public void clear() {
        copies.clear();
    }
}
