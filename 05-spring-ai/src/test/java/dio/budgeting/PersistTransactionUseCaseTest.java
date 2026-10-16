package dio.budgeting;

import dio.budgeting.application.InvalidTransactionException;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersistTransactionUseCaseTest {
    private TransactionRepository transactionRepository;
    private PersistTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        useCase = new PersistTransactionUseCase(transactionRepository);
    }

    @Test
    void savesTransactionWithValidDescriptionAndPositiveAmount() {
        var input = new PersistTransactionInput("Mercado", 1250, Category.GROCERIES);
        var transaction = new Transaction("Mercado", 1250, Category.GROCERIES);
        when(transactionRepository.save(org.mockito.ArgumentMatchers.any(Transaction.class)))
                .thenReturn(transaction);

        var result = useCase.execute(input);

        assertThat(result.description()).isEqualTo("Mercado");
        verify(transactionRepository).save(org.mockito.ArgumentMatchers.any(Transaction.class));
    }

    @Test
    void rejectsBlankDescriptionWithoutSaving() {
        var input = new PersistTransactionInput("  ", 1250, Category.GROCERIES);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessage("A descrição da transação não pode ficar vazia.");
        verify(transactionRepository, never()).save(org.mockito.ArgumentMatchers.any(Transaction.class));
    }

    @Test
    void rejectsNullDescriptionWithoutSaving() {
        var input = new PersistTransactionInput(null, 1250, Category.GROCERIES);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(InvalidTransactionException.class);
        verify(transactionRepository, never()).save(org.mockito.ArgumentMatchers.any(Transaction.class));
    }

    @Test
    void rejectsZeroOrNegativeAmountWithoutSaving() {
        for (long amount : new long[]{0, -1}) {
            var input = new PersistTransactionInput("Mercado", amount, Category.GROCERIES);

            assertThatThrownBy(() -> useCase.execute(input))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessage("O valor da transação deve ser maior que zero.");
        }
        verify(transactionRepository, never()).save(org.mockito.ArgumentMatchers.any(Transaction.class));
    }
}
