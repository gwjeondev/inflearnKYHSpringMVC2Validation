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
    /*
        ErrorCodeMessage Level
            Level1: NotBlank.item.itemName
            Level2: NotBlank.itemName
            Level3: NotBlank.java.lang.String
            Level4: NotBlank
     */
    @NotBlank
    private String itemName;

    /*
        ErrorCodeMessage Level
            Level1: NotNull.item.price
            Level2: NotNull.price
            Level3: NotNull.java.lang.Integer
            Level4: NotNull
    */
    @NotNull //null 허용x
    /*
        ErrorCodeMessage Level
            Level1: Range.item.price
            Level2: Range.price
            Level3: Range.java.lang.Integer
            Level4: Range
    */
    @Range(min = 1000, max = 1000000) //1000~1000000사이의 값만 허용
    private Integer price;

    /*
        ErrorCodeMessage Level
            Level1: NotNull.item.price
            Level2: NotNull.price
            Level3: NotNull.java.lang.Integer
            Level4: NotNull
    */
    @NotNull //null 허용x
    /*
        ErrorCodeMessage Level
            Level1: Max.item.price
            Level2: Max.price
            Level3: Max.java.lang.Integer
            Level4: Max
    */
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
