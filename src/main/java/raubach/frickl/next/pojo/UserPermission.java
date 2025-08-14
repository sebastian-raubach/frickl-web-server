package raubach.frickl.next.pojo;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserPermission
{
	private String name;
	private Integer code;
}
