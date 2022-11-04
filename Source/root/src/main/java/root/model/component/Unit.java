package root.model.component;

import java.util.List;

import root.model.action.attack.Attack;
import root.model.action.move.Move;
import root.model.event.ModelEventHandler;

public interface Unit {

	UnitType getUnitType();

	// Field getField();

	// void setField(Field field);

	boolean canMove();

	Move getMove();

	// void relocateTo(Field nextField);

	List<Attack> getAttacks();

	boolean hasAttack();

}
