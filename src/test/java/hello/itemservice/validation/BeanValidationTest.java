package hello.itemservice.validation;

import hello.itemservice.domain.item.Item__;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class BeanValidationTest {

    @Test
    void beanVlidation() {
        //검증기 생성
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Item__ item = new Item__();
        item.setItemName(" ");
        item.setPrice(0);
        item.setQuantity(10000);

        Set<ConstraintViolation<Item__>> violations = validator.validate(item);
        //값이 있으면 에러이고 없으면 에러아님.
        for (ConstraintViolation<Item__> violation : violations) {
            System.out.println("violation = " + violation);
            System.out.println("violation = " + violation.getMessage());
        }
    }
}
