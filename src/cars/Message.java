package cars;

import java.io.Serializable;

public class Message implements Serializable{
	private MsgType type;
	private Object data;
}
