package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Slf4j
@Component
public class ItemValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
       /*
       parameter로 넘어오는 clazz가 Item Class를 지원하는가?
       item == subItem: item을 상속하는 자식 class도 가능.
       */
        return Item.class.isAssignableFrom(clazz);
    }

    /**
     * @param target
     * @param errors BindingResult의 부모 Class
     */
    @Override
    public void validate(Object target, Errors errors) {
        Item item = (Item) target; //object -> item cast

        if(!StringUtils.hasText(item.getItemName())) {
            errors.rejectValue("itemName", "required");
        }
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            errors.rejectValue("price", "range", new Object[]{"1,000", "1,000,000"}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            errors.rejectValue("quantity", "max", new Object[]{"9,999"}, null);
        }
        if(item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                errors.reject("totalPriceMin", new Object[]{"10,000", resultPrice}, null);
            }
        }
    }

}
