package hello.itemservice.domain.item;

import lombok.Data;

//hibernate에서만 제공하는 validation, 즉 hibernate를 구현체로하는 validation만 사용가능.
import org.hibernate.validator.constraints.Range;

//javax에서 제공하는 표준 validation. 구현체에 관계없이 제공되는 표준 인터페이스.
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class Item {

    private Long id;

    //@NotBlank(message = "공백X") //빈값 + 공백만 있는경우를 허용x
    @NotBlank
    private String itemName;

    @NotNull //null 허용x
    @Range(min = 1000, max = 1000000) //1000~1000000사이의 값만 허용
    private Integer price;

    @NotNull //null 허용x
    @Max(9999) //최대가 9999
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
