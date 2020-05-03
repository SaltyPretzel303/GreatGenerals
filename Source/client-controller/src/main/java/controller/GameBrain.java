package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import controller.action.DefaultModelEventHandler;
import controller.option.AddToPathFieldOption;
import controller.option.MoveFieldOption;
import controller.option.SelectPathFieldOption;

import root.ActiveComponent;
import root.command.BasicCommandProcessor;
import root.command.Command;
import root.command.CommandProcessor;
import root.command.CommandProducer;
import root.command.CommandQueue;
import root.communication.ServerProxy;
import root.controller.Controller;
import root.model.Model;
import root.model.component.Field;
import root.model.component.option.FieldOption;
import root.model.event.ModelEventArg;
import root.view.View;
import root.view.event.ViewEventArg;
import root.view.event.ViewEventHandler;
import root.view.menu.Menu;
import view.command.SelectFieldCommand;
import view.command.ShowFieldInfoCommand;
import view.command.ZoomInCommand;
import view.command.ZoomOutCommand;

public class GameBrain implements Controller {

	private ServerProxy server_proxy;

	private ExecutorService serverCommandExecutor;
	private CommandProcessor serverCommandProcessor;
	private CommandQueue serverCommandQueue;

	private View view;
	private CommandQueue viewCommandQueue;

	private Model model;

	private Field selectedField;
	private Field focusedField;
	private List<Command> toUndo;

	private List<FieldOption> fieldOptions;

	// constructors
	public GameBrain(ServerProxy server_proxy, View view, Model model) {
		super();

		this.view = view;
		this.model = model;
		this.server_proxy = server_proxy;

		this.toUndo = new ArrayList<Command>();

		// attention let's say that every controller implementations has its own
		// ModelEventHandler (maybe this isn't the best approach)
		this.model.setEventHandler(new DefaultModelEventHandler(this));

		this.initFieldOptions();

		// --- connect serverProxy and controller

		this.serverCommandQueue = ((CommandProducer) this.server_proxy).getConsumerQueue();

		this.serverCommandExecutor = Executors.newSingleThreadExecutor();
		this.serverCommandProcessor = new BasicCommandProcessor(this.serverCommandExecutor, this);
		this.serverCommandQueue.setCommandProcessor(this.serverCommandProcessor);

		// --- done with serverProxy

		this.viewCommandQueue = this.view.getCommandQueue();

		// view events, click, key press ...
		this.initViewEventHandlers();

		this.view.show();

	}

	// methods

	private void initViewEventHandlers() {

		this.view.addEventHandler("left-mouse-click-event", new ViewEventHandler() {

			public void execute(ViewEventArg arg) {

				Field focused_field = model.getField(arg.getFieldPosition());

				if (focused_field != null) {

					// undo all previous commands
					if (!toUndo.isEmpty()) {
						for (int i = (toUndo.size() - 1); i >= 0; i--) {
							viewCommandQueue.enqueue(toUndo.get(i).getAntiCommand());
						}
					}
					toUndo.clear();

					// execute new command
					Command select_command = new SelectFieldCommand(focused_field);
					viewCommandQueue.enqueue(select_command);

					toUndo.add(select_command);

					selectedField = focused_field;
					focusedField = null;// note focusedField != focused_field

				}

			}

		});

		this.view.addEventHandler("right-mouse-click-event", new ViewEventHandler() {

			public void execute(ViewEventArg arg) {

				focusedField = model.getField(arg.getFieldPosition());

				// valid click
				if (focusedField != null) {

					Command showMenuCommand;
					if (selectedField != null) {
						showMenuCommand = new ShowFieldInfoCommand(selectedField, focusedField);
					} else {
						showMenuCommand = new ShowFieldInfoCommand(focusedField, focusedField);
					}

					viewCommandQueue.enqueue(showMenuCommand);

					toUndo.add(showMenuCommand);

				}

			}

		});

		// TODO maybe for the purpose of redrawing redraeing path and similar options
		// add additional list of command which are "stateless" and which execution wont
		// do any damage to the current state if they are executed more than once

		this.view.addEventHandler("key-event-char-1", new ViewEventHandler() {

			public void execute(ViewEventArg arg) {

				ZoomInCommand command = new ZoomInCommand(model.getFields());
				viewCommandQueue.enqueue(command);

				// this wont be valid in situation when attacak and build commands get
				// implemented
				// // reset old state
				// for (Command prev_command : toUndo) {
				// viewCommandQueue.enqueue(prev_command);
				// }

			}
		});

		this.view.addEventHandler("key-event-char-2", new ViewEventHandler() {

			public void execute(ViewEventArg arg) {

				ZoomOutCommand command = new ZoomOutCommand(model.getFields());
				viewCommandQueue.enqueue(command);

				// this wont be valid in sitation when attack and build command get implemented
				// // reset old state
				// for (Command prev_command : toUndo) {
				// viewCommandQueue.enqueue(prev_command);
				// }

			}
		});

	}

	private void initFieldOptions() {

		this.fieldOptions = new ArrayList<FieldOption>();

		this.fieldOptions.add(new SelectPathFieldOption(this));
		this.fieldOptions.add(new MoveFieldOption(this));
		this.fieldOptions.add(new AddToPathFieldOption(this));

	}

	// getters and setters

	@Override
	public View getView() {
		return this.view;
	}

	@Override
	public void setView(View view) {
		this.view = view;
	}

	@Override
	public Model getModel() {
		return this.model;
	}

	@Override
	public void setModel(Model model) {
		this.model = model;
	}

	@Override
	public void shutdown() {

		if (this.serverCommandExecutor != null && !this.serverCommandExecutor.isShutdown()) {
			this.serverCommandExecutor.shutdown();
		}

		if (this.view != null) {
			((ActiveComponent) this.view).shutdown();
		}

		if (this.model != null) {
			this.model.shutdown();
		}

	}

	@Override
	public void execute(ModelEventArg event_argument) {
		this.server_proxy.sendIntention(event_argument);
	}

	@Override
	public ServerProxy getServerProxy() {
		return this.server_proxy;
	}

	@Override
	public void setServerProxy(root.communication.ServerProxy new_proxy) {
		this.server_proxy = new_proxy;
	}

	@Override
	public CommandQueue getCommandQueue() {
		return this.serverCommandQueue;
	}

	@Override
	public void setCommandQueue(CommandQueue new_queue) {
		this.serverCommandQueue = new_queue;
	}

	@Override
	public CommandProcessor getCommandProcessor() {
		return this.serverCommandProcessor;
	}

	public void setConsumerQueue(CommandQueue consumer_queue) {
		this.viewCommandQueue = consumer_queue;
	}

	@Override
	public CommandQueue getConsumerQueue() {
		return this.viewCommandQueue;
	}

	@Override
	public List<FieldOption> getPossibleFieldOptions() {
		return this.fieldOptions;
	}

	@Override
	public void enqueueForUndone(Command new_command) {
		this.toUndo.add(new_command);
	}

	@Override
	public Field getSelectedField() {
		return this.selectedField;
	}

	@Override
	public void selectField(Field fieldToSelect) {

		this.selectedField = fieldToSelect;
		viewCommandQueue.enqueue(new SelectFieldCommand(this.selectedField));

		Menu fieldMenu = view.getOptionMenu();
		if (fieldMenu.isDisplayed()) {
			selectedField.adjustOptionsFor(focusedField);
			fieldMenu.populateWith(selectedField.getEnabledOptions());
		}

	}

}
