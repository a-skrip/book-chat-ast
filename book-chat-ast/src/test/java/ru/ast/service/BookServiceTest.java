package ru.ast.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ast.dto.response.BookResponseDto;
import ru.ast.entity.Book;
import ru.ast.enums.BookStatus;
import ru.ast.exceptions.BookNotFoundException;
import ru.ast.repository.BookRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;

    @InjectMocks
    private BookService service ;

    private UUID uuid;
    private Book book;
    private BookResponseDto expectedDto;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();

        book = new Book();
        book.setId(uuid);
        book.setTitle("Герой нашего времени");
        book.setFullText("Полный текст книги...");
        book.setUploadPath("/input/file/test.txt");
        book.setStatus(BookStatus.UPLOADED);
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getBook_ShouldReturnBookResponseDto_WhenBookExists() {
        //arrange
        when(repository.findById(uuid)).thenReturn(Optional.of(book));
        //act
        BookResponseDto responseDto = service.getBook(uuid);
        //assert
        assertThat(responseDto)
                .isNotNull()
                .satisfies(dto -> {
                    assertThat(dto.getId()).isEqualTo(uuid);
                    assertThat(dto.getTitle()).isEqualTo(book.getTitle());
                    assertThat(dto.getFullText()).isEqualTo(book.getFullText());
                    assertThat(dto.getUploadPath()).isEqualTo(book.getUploadPath());
                });
        verify(repository).findById(uuid);
    }

    @Test
    void getBook_ShouldThrowBookNotFoundException_WhenBookNotFound() {
        //arrange
        UUID nonExistentUuid = UUID.randomUUID();
        when(repository.findById(nonExistentUuid)).thenReturn(Optional.empty());
        //act assert
        assertThatThrownBy(() -> service.getBook(nonExistentUuid))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Книга c id: " + nonExistentUuid + " не существует в системе");
    }

}