package adf.component.communication;

import java.util.List;

abstract public class MessageBundle {
	abstract public List<Class<? extends CommunicationMessage>> getMessageClassList();
}
