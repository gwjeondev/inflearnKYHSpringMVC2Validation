package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import hello.itemservice.web.validation.form.ItemSaveForm;
import hello.itemservice.web.validation.form.ItemUpdateForm;
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
@RequestMapping("/validation/v5/items")
@RequiredArgsConstructor
public class ValidationItemControllerV5 {

    //생성자가 1개이면 @Autowired가 생략가능하며, Lombok의 RequiredArgsConstrctor가 생성자를 자동으로 생성해준다.
    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v5/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v5/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v5/addForm";
    }

    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {
        
        //특정 필드가 아닌 복합 룰 검증... BeanValidation도 ObjectError를 검증할 수 있으나 기능이 많이 약하고 제약사항이 많다. 자세한 내용은 Item.class 파일 참조
        if(form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{"10,000", resultPrice}, null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v5/addForm";
        }

        //성공 로직
        Item item = new Item(form.getItemName(), form.getPrice(), form.getQuantity());
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        //addAttribute에 등록한 itemId가 redirect url에 포함될 경우 PathVariable로 redirect 처리 controller에서 사용 가능
        //redirect url에 포함되지 않을 경우 queryParameter로 전달됨. status 같은 경우는 queryParameter로 전달된다.
        return "redirect:/validation/v5/items/{itemId}";
    }



    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v5/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {

        //특정 필드가 아닌 복합 룰 검증... BeanValidation도 ObjectError를 검증할 수 있으나 기능이 많이 약하고 제약사항이 많다. 자세한 내용은 Item.class 파일 참조
        if(form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if(resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{"10,000", resultPrice}, null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if(bindingResult.hasErrors()) {
            log.debug("errors = {}", bindingResult);
            return "validation/v5/editForm";
        }

        Item itemParam = new Item(form.getItemName(), form.getPrice(), form.getQuantity());
        itemRepository.update(itemId, itemParam);
        return "redirect:/validation/v5/items/{itemId}";
    }



}

