package hello.itemservice.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

import static org.assertj.core.api.Assertions.*;

public class MessageCodesResolverTest {

    MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();
    /*
    DefaultMessageCodesResolver는 다음과 같은 메시지 생성 규칙에 따라 동작한다.
    - 객체오류
        1. code + "." + object name
        2. code
        ex) code: required, object name: item
            1. required.item
            2. required

    - 필드오류
        1. code + "." + object name + "." + field
        2. code + "." + field
        3. code + "." + field type
        4. code
        ex) code: typeMismatch, object name: user, field: age, field type: int
            1. typeMismatch.user.age
            2. typeMismatch.age
            3. typeMismatch.int
            4. typeMismatch
    */

    @Test
    void messageCodesResolverObject() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");

        //순서를 포함하여 장확히 일치
        assertThat(messageCodes).containsExactly("required.item", "required");
    }

    @Test
    void messageCodesResolverField() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item", "itemName", String.class);

        //순서를 포함하여 장확히 일치
        assertThat(messageCodes).containsExactly("required.item.itemName", "required.itemName",
                "required.java.lang.String", "required");
    }
}
