package com.commic.v1.services.book;

import com.commic.v1.dto.requests.BookRequest;
import com.commic.v1.dto.responses.APIResponse;
import com.commic.v1.dto.responses.BookResponseDTO;
import com.commic.v1.entities.Book;
import com.commic.v1.entities.Category;
import com.commic.v1.mapper.BookMapper;
import com.commic.v1.repositories.IBookRepository;
import com.commic.v1.repositories.ICategoryRepository;
import com.commic.v1.repositories.IChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BookService implements IBookService {
    @Autowired
    private IBookRepository bookRepository;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private IChapterRepository chapterRepository;
    @Autowired
    private ICategoryRepository categoryRepository;

    @Override
    public APIResponse<Void> addBook(BookRequest bookRequest) {
        Book book = bookMapper.toBook(bookRequest);
        // check exist book name
        Example<Book> example = Example.of(Book.builder().name(book.getName()).build());
        if (bookRepository.exists(example)) {
            return new APIResponse<>(HttpStatus.CONFLICT.value(), "Book name already exists", null);
        }

        try {
            Set<Category> categories = categoryRepository.findByNameIn(bookRequest.getCategoryNames());
            book.setCategories(categories);

            book = bookRepository.save(book);
            // Add this book to each category's set of books
            for (Category category : categories) {
                category.getBooks().add(book);
            }
            categoryRepository.saveAll(categories);
            return new APIResponse<>(HttpStatus.NO_CONTENT.value(), "Add book successfully", null);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return new APIResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Add book failed", null);
        }
    }

    @Override
    public BookResponseDTO getDescription(Integer id) {
        try {
            Book book = bookRepository.findById(id).get();
            if (book != null) {
                Integer quantityChapter = Optional.ofNullable(chapterRepository.countByBookId(book.getId())).orElse(0);
                Integer views = Optional.ofNullable(chapterRepository.countViewByBookId(book.getId())).orElse(0);
                Double starAvg = Optional.ofNullable(chapterRepository.countStarAvgByBookId(book.getId())).orElse(0.0);
                Date publishDate = chapterRepository.findFirstPublishDateByBookId(book.getId());
                List<String> categories = bookRepository.findCategoryNamesByBookId(book.getId());
                BookResponseDTO bookResponseDTO = bookMapper.toBookResponseDTO(book);
                bookResponseDTO.setQuantityChapter(quantityChapter);
                bookResponseDTO.setView(views);
                bookResponseDTO.setRating(starAvg);
                bookResponseDTO.setCategoryNames(categories);
                bookResponseDTO.setPublishDate(publishDate);
                return bookResponseDTO;
            } else {
                return new BookResponseDTO();
            }
        } catch (DataAccessException ex) {
            ex.printStackTrace();
            return new BookResponseDTO();
        }
    }


}
