package root.communication.messages.components;

import java.util.List;

import root.communication.parser.StaticParser;

public class UnitDesc {

	// TODO unit name shoud be enum
	public String unitName;
	public String moveType;
	public List<String> attacks;

	public String defense;

	public int health;

	public UnitDesc() {

	}

	@Override
	public String toString() {
		return StaticParser.ToString(this);
	}

}
