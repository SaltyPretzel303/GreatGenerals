package controller.option;

import root.controller.Controller;
import root.model.component.Field;
import root.model.component.option.FieldOption;

public class AttackFieldOption extends FieldOption {

	public static final String Name = "attack-field-option";

	public AttackFieldOption(Controller gameController) {
		super(AttackFieldOption.Name, gameController);
	}

	public AttackFieldOption(boolean enabled, Controller controller, Field primaryField) {
		super("attack-field-option", enabled, controller, primaryField);
	}

	@Override
	public void run() {
		// show a new submenu with attack types if more than one is available 
		// or maybe list them in the description ara together with the descriptions 
		// for each, and also with the option to 'execute' them
	}

	@Override
	public FieldOption getCopy() {

		return new AttackFieldOption(true, this.controller, null);

	}

	@Override
	public boolean isAdequateFor(Field field) {
		return (field.getUnit() != null && field.getUnit().hasAttack());
	}

}
