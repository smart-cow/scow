package amqp.translators;

import java.util.Map;

import amqp.AmqpMessage;

public class XmppMessageFromProperties implements ITranslateAmqpMessage {
	
	private String formatString_;
	
	public XmppMessageFromProperties(String format) {
		formatString_ = format;
	}
	
	@Override
	public String translate(AmqpMessage message) {
		
		StateContext sc = new StateContext(message.getVariables());
		for (char c : formatString_.toCharArray()) {
			sc.parse(c);
		}
		return sc.getMessage().toString();
	}
	
	
	public String getFormatString() {
		return formatString_;
	}
	
	public void setFormatString(String formatString) {
		formatString_ = formatString;
	}
	
	
	
	
	private interface IState {
		void parse(StateContext sc, char c);
	}
	
	
	
	private class StateContext {
		private IState state_ = new Regular();
		private StringBuilder translatedMessage_ = new StringBuilder();
		private Map<String, String> varValues_;
		
		public StateContext(Map<String, String> varValues) {
			varValues_ = varValues;
		}
		
		public void setState(IState state) {
			state_ = state;
		}
		
		public void parse(char c) {
			state_.parse(this, c);
		}
		
		public void appendToMessage(char c) {
			translatedMessage_.append(c);
		}
		
		
		public StringBuilder getMessage() {
			return translatedMessage_;
		}
		
		public void appendVar(String varName) {
			translatedMessage_.append(varValues_.get(varName));
		}
	}
	
	
	private class Regular implements IState {	
		public void parse(StateContext sc, char c) {
			switch(c) {
			case '$':
				sc.setState(new VarName());
				break;			
			case '\\':
				sc.setState(new Escape(sc.getMessage(), this));
				break;
			default:
				sc.appendToMessage(c);
			}
		}
	}
	
	
	
	private class Escape implements IState {
		private StringBuilder charDest_;
		private IState lastState_;

		public Escape(StringBuilder charDest, IState lastState) {
			charDest_ = charDest;
			lastState_ = lastState;
		}
		
		public void parse(StateContext sc, char c) {
			charDest_.append(c);
			sc.setState(lastState_);
		}
	}
	
	
	private class VarName implements IState {
		StringBuilder varName_ = new StringBuilder();
		public void parse(StateContext sc, char c) {
			switch (c) {
			case '$':
				sc.appendToMessage('$');
				sc.setState(new Regular());
			case '{':
				break;
			case '\\':
				sc.setState(new Escape(varName_, this));
				break;
			case '}':
				sc.appendVar(varName_.toString());
				sc.setState(new Regular());
				break;
			default:
				varName_.append(c);
			}
		}
		
	}


}
