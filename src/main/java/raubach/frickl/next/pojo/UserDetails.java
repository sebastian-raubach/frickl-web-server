package raubach.frickl.next.pojo;

import lombok.*;
import lombok.experimental.Accessors;
import raubach.frickl.next.codegen.tables.pojos.Users;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@ToString
public class UserDetails extends Users
{
	private boolean canBeEdited;
}
