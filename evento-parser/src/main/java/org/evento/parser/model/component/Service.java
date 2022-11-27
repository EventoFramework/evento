package org.evento.parser.model.component;

import org.evento.parser.model.handler.ServiceCommandHandler;

import java.util.List;

public class Service extends Component {
	private List<ServiceCommandHandler> commandHandlers;

	public void setCommandHandlers(List<ServiceCommandHandler> commandHandlers) {
		this.commandHandlers = commandHandlers;
	}

	public List<ServiceCommandHandler> getCommandHandlers() {
		return commandHandlers;
	}
}
