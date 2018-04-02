package cz.zelenikr.remotetouch.data.command;

import cz.zelenikr.remotetouch.data.message.MessageContent;

/**
 * Represents command from/to server.
 *
 * @author Roman Zelenik
 */
public class CommandDTO implements MessageContent {

    private Command cmd;
    private String output;

    public CommandDTO(Command cmd) {
        this.cmd = cmd;
        this.output = "";
    }

    public Command getCmd() {
        return cmd;
    }

    public void setCmd(Command cmd) {
        this.cmd = cmd;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommandDTO{");
        sb.append("cmd=").append(cmd);
        sb.append(", output='").append(output).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
