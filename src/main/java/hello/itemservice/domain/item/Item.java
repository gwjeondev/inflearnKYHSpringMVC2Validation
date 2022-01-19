package hello.itemservice.domain.item;

import lombok.Data;
//hibernate에서만 제공하는 validation, 즉 hibernate를 구현체로하는 validation만 사용가능.
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.ScriptAssert;
//javax에서 제공하는 표준 validation. 구현체에 관계없이 제공되는 표준 인터페이스.
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
/*
    실행해보면 정상 수행되는 것을 확인할 수 있다. 메시지 코드도 다음과 같이 생성된다.
        ScriptAssert.item
        ScriptAssert
    그런데 실제 사용해보면 제약이 많고 복잡하다. 그리고 실무에서는 검증 기능이 해당 객체의 범위를 넘어서는 경우들도 종종 등장하는데, 그런 경우 대응이 어렵다.
    따라서 오브젝트 오류(글로벌 오류)의 경우 @ScriptAssert 을 억지로 사용하는 것 보다는 오브젝트 오류 관련 부분만 직접 자바 코드로 작성하는 것을 권장한다.
*/
/*@ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >= 10000",
message = "총합이 10,000원 넘게 입력해주세요.")*/
public class Item {
    /*
    ErrorCodeMessage Level Annotation별로 공통이다.
        Level1: (Annotation명).item.itemName ex)NotBlank.item.itemName
        Level2: (Annotation명).itemName
        Level3: (Annotation명).java.lang.String, Integer 타입일 경우 NotBlank.java.lang.Integer
        Level4: (Annotation명)
    */
    @NotNull(groups = UpdateCheck.class)
    private Long id;

    @NotBlank(groups = {UpdateCheck.class, SaveCheck.class})
    private String itemName;

    @NotNull(groups = {UpdateCheck.class, SaveCheck.class})
    @Range(min = 1000, max = 1000000, groups = {UpdateCheck.class, SaveCheck.class}) //1000~1000000사이의 값만 허용
    private Integer price;

    @NotNull(groups = {UpdateCheck.class, SaveCheck.class})
    @Max(value = 9999, groups = SaveCheck.class) //수정 요구사항 추가
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
