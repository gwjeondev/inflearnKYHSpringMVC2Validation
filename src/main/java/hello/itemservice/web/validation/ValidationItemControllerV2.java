package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    //생성자가 1개이면 @Autowired가 생략가능하며, Lombok의 RequiredArgsConstrctor가 생성자를 자동으로 생성해준다.
    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

    //@PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult,
                          RedirectAttributes redirectAttributes, Model model) {

        //검증 로직
        if(!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
        }

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }

        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                //field error가 아닌 item object 자체에 대한 global 오류 임으로 objectError로 생성한다.
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이여야 합니다. 현재 값 = " + resultPrice));
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //검증 error시에도 기존 값 유지하는 v2 버전.
    //@PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {
        //검증 로직
        if(!StringUtils.hasText(item.getItemName())) {
            /*
            rejectedValue: 사용자가 입력하여 거부당한 값, 즉 사용자 입력 값.
            bindingFailure: 입력한 값이 바인딩 되기전에 fail이 일어났는지에 대한 boolean Check, 예로 숫자 타입인데 문자가 들어왔을경우 true.
            숫자는 문자로 바인딩될수 없기 때문에 바인딩 되기전에 fail이 일어남.
            */
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),
                    false, null, null, "상품 이름은 필수입니다."));
        }
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(),
                    false, null, null, "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(),
                    false, null, null, "수량은 최대 9,999 까지 허용합니다."));
        }

        /*
        위의 검증 외에도 만약 price에 int가 아닌 문자가 들어온다면 타입에러가 발생하는데, 스프링에서는 해당 컨트롤러 "addItemV2"를 실행하기 전에,
        BindingResult에다가 위에 코드로 만들어진 것 처럼 addError(new FieldError(....))를 그대로 수행해준다.
        그후 타입에러에 대한 검증이 처리된후에 스프링은 컨트롤러를 호출한다.
        때문에 타입에러의 경우 위와 같이 코드를 작성하지 않아도 에러 메시지가 담기고, 사용자가 작성한 데이터가 그대로 유지될 수 있다.
        bindingResult.addError(new FieldError("item", "price", "ㅁㄹㄴㅇㄹ", true, null, null, 타입 에러 메시지~~.....~));
        */

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                //field error가 아닌 item object 자체에 대한 global 오류 임으로 objectError로 생성한다.
                bindingResult.addError(new ObjectError("item", null, null, "가격 * 수량의 합은 10,000원 이상이여야 합니다. 현재 값 = " + resultPrice));
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //errors.properties를 이용한 일관성있는 에러 메시지 관리.
    //@PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {

        //검증 로직
        if(!StringUtils.hasText(item.getItemName())) {
            /*codes를 String 배열로 받는 이유는 첫번째 인덱스 code가 없으면 두번째 인덱스 code를 찾는식으로 진행된다.*/
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),
                    false, new String[]{"testNotValue", "required.item.itemName"}, null, "code 배열의 key가 없으면 이부분이 출력된다."));
        }
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(),
                    false, new String[]{"range.item.price"}, new Object[]{"1,000", "1,000,000"}, null));
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(),
                    false, new String[]{"max.item.quantity"}, new Object[]{"9,999"}, null));
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                //field error가 아닌 item object 자체에 대한 global 오류 임으로 objectError로 생성한다.
                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"},
                        new Object[]{"10,000", resultPrice}, null));
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //rejectValue(), reject()를 이용한 new ObjectError(), new FieldError()가 했던 일을 그대로 단순화하며 코드를 줄일수 있다.
    //@PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {

        /*
        bindingResult에 rejectValue 하기전 Errors가 존재 한다는 것은 타입에러이므로 그냥 바로 return 처리한다.
        이렇게 하면
        예를들어 타입에러인경우 object의 변수에는(여기서는 item의 price라고 가정) 당연히 null이 들어가 있을 것이고,
        그렇데 된다면 밑의 검증 로직에서도 에러 메시지가 추가 되기 때문에 한 field에 대해 2개의 에러메시지가 생길 수 있는데
        이를 방지 할 수 있다.
        이렇게 처리하는 경우도 많다고 한다.
        */
        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

       /* 사실 bindingResult는 반드시 자신을 바인딩할 Object인 ModelAttribute 뒤에 위치해야 하는데,
        이는 자신을 바인딩 할 객체를 사실 알고 있다는것과 같은 의미이다. 다음과 같이 log를 출력해보면 item 객체를 가르킨다는것을 알 수 있다.
        따라서 아래의 rejectValue를 통한 검증시 target object인 item에 대한 명시는 new FieldError()에서 사용했던것과 다르게 명시할 필요가 없다. */
        log.info("objectName = {}", bindingResult.getObjectName()); // --> objectName = item
        log.info("target = {}", bindingResult.getTarget()); // --> target = Item(id=null, itemName=2, price=33, quantity=1)


        //아래 검증로직을 다음과 같이 한줄로 줄일 수 있다. if문을 통하여 값이 있나 없나 검증하지 않고 단순히 empty인지 공백인지에 대해 간단히 처리할 때 유용하다.
        //ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");

        //검증 로직
        if(!StringUtils.hasText(item.getItemName())) {
            /*
            1. errorCode인 required는 메시지에 등록된 코드가 아니다. 뒤에서 설명할 MessageCodesResolver를 위한 오류코드이다.
            2. 스프링은 errorCode에 등록된 Code를 확인하고 errors.properties에 required가 있는지 찾는다.
            3. 만약 required 밖에 선언되어 있지 않다면 required의 value를 가져온다.
            4. 하지만 required.item.itemName[ex) errorCode(required).objectName.fieldName]과 같이 조금 더 상세한 key값이 있다면 해당 key값의 value를 가져온다.
                실제로 생성되는 key값들은 다음과 같다. 위에서 부터 아래로 우선순위가 약해진다.
                - required.item.itemName
                - required.itemName
                - required.java.lang.String
                - required
            5. 일반적으로 범용적인 required 코드를 사용하다가 조금 더 상세한 메세지가 필요 할 때에는 상세한 코드를 선언하여 사용하면 된다.
            6. 우선순위는 상세한 code > 단순한 code라고 생각하면 쉽다.
            7. 한마디로 V3에서 사용했던 다음과 같은 코드의 new String[]{....} 역할을 대신 해주는 것이다.
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),
                    false, new String[]{"required.item.itemName", "required"}, null, null));
            8. 어쨌든 스프링에서는 MessageCodesResolver가 변환한 code들을 타임리프로 전송하는데 타임리프에서 th:errors가 실행되면서
                만약 이때 오류가 있다면 생성된 code들을 순서대로 돌아가면서 errors.properties에서 메시지를 찾는다. 그리고 없으면 디폴트 메시지를 출력한다.
            9. 자세한 내용은 Test의 MessageCodesResolverTest.java를 참고하자.
            */
            bindingResult.rejectValue("itemName", "required");
        }

        /*
        만약 타입 에러시 스프링이 컨트롤러를 호출하기전에 자동으로 BindingResult에다가 타입에러에 관한 rejectValue를 싣는데,
        이때 errorCode는 "typeMismatch"가 default로 삽입되며,
        level1: typeMismatch.item.price
        level2: typeMismatch.price
        level3: typeMismatch.java.lang.Integer
        level4: typeMismatch
        가 errorCode로 타임리프에 전달된다. 하지만 이때 위의 level1부터 level4까지 errors.properties에 저장 되어 있지 않으니
        타임리프에서는 스프링이 삽입해준 defaultMessage를 출력하게 되는것이다. 이런 메시지는 개발자나 알아먹을수 있지
        사용자에게는 불쾌한 에러 메시지로, 이에 대한 error 메시지에 대해서도 따로 정의를 해두고 사용하도록 하자.
        */
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.rejectValue("price", "range", new Object[]{"1,000", "1,000,000"}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.rejectValue("quantity", "max", new Object[]{"9,999"}, null);
        }

        //특정 필드가 아닌 복합 룰 검증
        if(item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{"10,000", resultPrice}, null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //validation 검증을 class를 만들어 따로 분리한다.
    @PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {

        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        if(itemValidator.supports(Item.class)) {
            itemValidator.validate(item, bindingResult);
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

