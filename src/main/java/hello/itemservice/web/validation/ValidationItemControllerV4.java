package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
public class ValidationItemControllerV4 {

    //생성자가 1개이면 @Autowired가 생략가능하며, Lombok의 RequiredArgsConstrctor가 생성자를 자동으로 생성해준다.
    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v4/addForm";
    }

    /*
    1. 스프링 MVC는 어떻게 Bean Validator를 사용?
        스프링 부트가 spring-boot-starter-validation 라이브러리를 넣으면 자동으로 Bean Validator를 인지하고 스프링에 통합한다.

    2. 스프링 부트는 자동으로 글로벌 Validator로 등록한다.
        LocalValidatorFactoryBean 을 글로벌 Validator로 등록한다. 이 Validator는 @NotNull 같은
        애노테이션을 보고 검증을 수행한다. 이렇게 글로벌 Validator가 적용되어 있기 때문에, @Valid, @Validated 만 적용하면 된다.
        검증 오류가 발생하면, FieldError, ObjectError 를 생성해서 BindingResult 에 담아준다.

    3. 주의점
        직접 글로벌 Validator를 직접 등록하면 스프링 부트는 Bean Validator를 글로벌 Validator 로 등록하지 않는다.
        따라서 애노테이션 기반의 빈 검증기가 동작하지 않는다. 다음 부분은 제거하자.

    4. 검증 순서
        1. @ModelAttribute 각각의 필드에 타입 변환 시도
            1. 성공하면 다음으로
            2. 실패하면 typeMismatch 로 FieldError 추가
        2. Validator 적용

    5. 바인딩에 성공한 필드만 Bean Validation 적용
        BeanValidator는 바인딩에 실패한 필드는 BeanValidation을 적용하지 않는다.
        생각해보면 타입 변환에 성공해서 바인딩에 성공한 필드여야 BeanValidation 적용이 의미 있다.
        (일단 모델 객체에 바인딩 받는 값이 정상으로 들어와야 검증도 의미가 있다.)
        @ModelAttribute 각각의 필드 타입 변환시도 변환에 성공한 필드만 BeanValidation 적용

        예)
        itemName 에 문자 "A" 입력 -> 타입 변환 성공  -> itemName 필드에 BeanValidation 적용
        price 에 문자 "A" 입력 -> "A"를 숫자 타입 변환 시도 실패 -> typeMismatch FieldError 추가
        -> price 필드는 BeanValidation 적용 X
     */

    //@Validated 애노테이션이 있으면 Bean Validation이 자동으로 적용된다. 즉 Item 객체는 Bean Validation을 사용하겠다는 의미이다.
    //@PostMapping("/add")
    public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        
        //특정 필드가 아닌 복합 룰 검증... BeanValidation도 ObjectError를 검증할 수 있으나 기능이 많이 약하고 제약사항이 많다. 자세한 내용은 Item.class 파일 참조
        if(item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{"10,000", resultPrice}, null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v4/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }

    @PostMapping("/add")
    public String addItem2(@Validated(SaveCheck.class) @ModelAttribute Item item, BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        /*
        - groups 기능 사용에 대해 @Validated(SaveCheck.class)
        groups 기능을 사용해서 등록과 수정시에 각각 다르게 검증이 가능하다. 그런데 groups 기능을 사용하니 Item은 물론이고 전반적으로 복잡도가 올라갔다.
        사실 groups 기능은 실제 잘 사용되지는 않는데, 그 이유는 실무에서는 주로 등록용 폼 객체와 수정용 폼 객체를 분리해서 사용하기 때문이다.
        */

        //특정 필드가 아닌 복합 룰 검증... BeanValidation도 ObjectError를 검증할 수 있으나 기능이 많이 약하고 제약사항이 많다. 자세한 내용은 Item.class 파일 참조
        if(item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{"10,000", resultPrice}, null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v4/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/editForm";
    }

    //@PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute Item item, BindingResult bindingResult) {

        //특정 필드가 아닌 복합 룰 검증... BeanValidation도 ObjectError를 검증할 수 있으나 기능이 많이 약하고 제약사항이 많다. 자세한 내용은 Item.class 파일 참조
        if(item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{"10,000", resultPrice}, null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v4/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v4/items/{itemId}";
    }

    @PostMapping("/{itemId}/edit")
    public String edit2(@PathVariable Long itemId, @Validated(UpdateCheck.class) @ModelAttribute Item item, BindingResult bindingResult) {
        /*
        - groups 기능 사용에 대해 @Validated(SaveCheck.class)
        groups 기능을 사용해서 등록과 수정시에 각각 다르게 검증이 가능하다. 그런데 groups 기능을 사용하니 Item은 물론이고 전반적으로 복잡도가 올라갔다.
        사실 groups 기능은 실제 잘 사용되지는 않는데, 그 이유는 실무에서는 주로 등록용 폼 객체와 수정용 폼 객체를 분리해서 사용하기 때문이다.
        */

        //특정 필드가 아닌 복합 룰 검증... BeanValidation도 ObjectError를 검증할 수 있으나 기능이 많이 약하고 제약사항이 많다. 자세한 내용은 Item.class 파일 참조
        if(item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{"10,000", resultPrice}, null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v4/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v4/items/{itemId}";
    }

}

