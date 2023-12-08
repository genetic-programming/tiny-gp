public class Interpreter {
    private final char[] program;
    private int pc;
    private final double[] xAxis;

    Settings settings;

    public Interpreter(char[] program, Settings settings, double[] xAxis) {
        this.program = program;
        this.pc = 0;
        this.settings = settings;
        this.xAxis = xAxis;
    }

    double run() {
        char primitive = program[pc++];
        if (primitive < settings.F_SET_START)
            return (xAxis[primitive]);
        switch (primitive) {
            case Settings.ADD:
                return (run() + run());
            case Settings.SUB:
                return (run() - run());
            case Settings.MUL:
                return (run() * run());
            case Settings.DIV: {
                double num = run(), den = run();
                if (Math.abs(den) <= 0.001)
                    return (num);
                else
                    return (num / den);
            }
            case Settings.SIN:
                return (Math.sin(run()));
            case Settings.COS:
                return (Math.cos(run()));
        }
        return (0.0); // should never get here
    }
}
