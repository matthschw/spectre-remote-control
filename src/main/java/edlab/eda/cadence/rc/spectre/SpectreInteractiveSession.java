package edlab.eda.cadence.rc.spectre;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edlab.eda.cadence.rc.api.GenericSkillCommandTemplates;
import edlab.eda.cadence.rc.api.IncorrectSyntaxException;
import edlab.eda.cadence.rc.api.SkillCommand;
import edlab.eda.cadence.rc.data.SkillBoolean;
import edlab.eda.cadence.rc.data.SkillDataobject;
import edlab.eda.cadence.rc.data.SkillFixnum;
import edlab.eda.cadence.rc.data.SkillFlonum;
import edlab.eda.cadence.rc.data.SkillList;
import edlab.eda.cadence.rc.data.SkillString;
import edlab.eda.cadence.rc.data.SkillSymbol;
import edlab.eda.cadence.rc.session.EvaluableToSkill;
import edlab.eda.cadence.rc.session.EvaluationFailedException;
import edlab.eda.cadence.rc.session.InvalidDataobjectReferenceExecption;
import edlab.eda.cadence.rc.session.SkillInteractiveSession;
import edlab.eda.cadence.rc.session.UnableToStartInteractiveSession;
import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.reader.nutmeg.NutReader;
import edlab.eda.reader.nutmeg.NutmegPlot;

/**
 * Interactive Session for Cadence Spectre
 */
public final class SpectreInteractiveSession extends SpectreSession {

  private Map<String, String> parameterMapping = new HashMap<>();
  private final Map<String, SkillDataobject> parameterValues = new HashMap<>();

  private List<String> analyses = null;
  private Map<String, String> analysesMapping;

  private final SkillInteractiveSession session;
  private Thread parentThread;

  /**
   * Create a new Spectre session
   *
   * @param factory Factory that instantiated this session
   * @param name    Name of the session
   */
  SpectreInteractiveSession(final SpectreFactory factory, final String name) {

    super(factory, name);

    this.parentThread = null;

    this.session = new SkillInteractiveSession(this.workingDir);
    this.session.setCommand(this.formatShellCommand());
    this.session.setPrompt(">");

    this.session.setTimeout(factory.getTimeoutDuration(),
        factory.getTimeoutTimeUnit());

    this.session.setWatchdogTimeout(factory.getWatchdogTimeoutDuration(),
        factory.getWatchdogTimeoutTimeUnit());
  }

  /**
   * Set the parent thread of the session
   *
   * @param thread parent thread
   * @return this
   */
  public SpectreInteractiveSession setParentThread(final Thread thread) {
    this.parentThread = thread;
    return this;
  }

  /**
   * Get the parent thread of the session
   *
   * @return thread parent thread
   */
  Thread getParentThread() {
    return this.parentThread;
  }

  @Override
  public SpectreInteractiveSession setNetlist(final String netlist) {

    boolean restart = false;

    this.netlist = netlist;

    if (this.session.isActive()) {
      this.session.stop();
      restart = true;
    }

    this.writeNetlist();

    if (restart) {
      try {
        this.session.start(this.parentThread);
      } catch (final Exception e) {
      }
    }

    return this;
  }

  @Override
  public SpectreInteractiveSession setNetlist(final File netlist)
      throws IOException {

    this.netlist = new String(Files.readAllBytes(netlist.toPath()),
        StandardCharsets.US_ASCII);

    boolean restart = false;

    if (this.session.isActive()) {
      this.session.stop();
      restart = true;
    }

    this.writeNetlist();

    if (restart) {
      try {
        this.session.start(this.parentThread);
      } catch (final Exception e) {

      }
    }
    return this;
  }

  @Override
  public boolean addIncludeDirectory(final File includeDirectory)
      throws FileNotFoundException {

    if ((includeDirectory != null) && includeDirectory.isDirectory()
        && includeDirectory.canRead()) {

      if (!this.isIncludeDirectory(includeDirectory)) {

        this.includeDirectories.add(includeDirectory);

        if (this.session.isActive()) {
          this.session.stop();
        }
        return true;
      }

      return false;

    } else {
      throw new FileNotFoundException(includeDirectory.getAbsolutePath());
    }
  }

  @Override
  public boolean removeIncludeDirectory(final File includeDirectory) {

    if (this.isIncludeDirectory(includeDirectory)) {

      if (this.isIncludeDirectory(includeDirectory)) {

        this.includeDirectories.remove(includeDirectory);

        if (this.session.isActive()) {
          this.session.stop();
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Format the shell command for simulation
   *
   * @return command for simulation
   */
  private String formatShellCommand() {

    final StringBuilder cmd = new StringBuilder()
        .append(this.factory.getCommand());

    if (this.mode == MODE.BIT64) {
      cmd.append(" -64");
    } else if (this.mode == MODE.BIT32) {
      cmd.append(" -32");
    }

    cmd.append(" +interactive");

    if (this.resultFmt == RESULT_FMT.NUTBIN) {
      cmd.append(" -format nutbin");
    } else if (this.resultFmt == RESULT_FMT.NUTASCII) {
      cmd.append(" -format nutascii");
    }

    if (this.noOfThreads > 1) {
      cmd.append(" +multithread=").append(this.noOfThreads);
    }

    cmd.append(" -ahdllibdir ./").append(SpectreSession.AHDLLIB_DIRNAME);
    cmd.append(" =log ").append(SpectreSession.LOG_FILENAME);

    for (final File file : this.includeDirectories) {
      cmd.append(" -I").append(file.getAbsolutePath());
    }

    cmd.append(" ").append(SpectreSession.getNetlistName());

    return cmd.toString();
  }

  /**
   * Start {@link SkillInteractiveSession}
   *
   * @return <code>true</code> when the session is started successfully,
   *         <code>false</code> otherwise
   * @throws UnableToStartSession when the session cannot be started
   */
  public SpectreInteractiveSession start() throws UnableToStartSpectreSession {

    if (!this.writeNetlist()) {
      throw new UnableToStartSpectreSession(this.formatShellCommand(),
          this.workingDir, new File(this.workingDir, SpectreSession.LOG_FILENAME));
    }

    try {

      this.session.setCommand(this.formatShellCommand());

      this.session.start(this.parentThread);
      this.analyses = null;
      this.parameterMapping = new HashMap<>();
      this.analysesMapping = null;

    } catch (final Exception e) {
      throw new UnableToStartSpectreSession(this.formatShellCommand(),
          this.workingDir, new File(this.workingDir, SpectreSession.LOG_FILENAME));
    }

    for (final Entry<String, SkillDataobject> param : this.parameterValues
        .entrySet()) {
      this.setValueAttribute(param.getKey(), param.getValue());
    }

    return this;
  }

  /**
   * Set a simulation parameter to a particular value
   *
   * @param parameter Name of parameter
   * @param value     Value as String
   * @return <code>true</code> when the parameter was changed successfully,
   *         <code>false</code> otherwise
   * @throws UnableToStartSession when the session cannot be started
   */
  public boolean setValueAttribute(final String parameter, final String value)
      throws UnableToStartSpectreSession {
    return this.setValueAttribute(parameter, new SkillString(value));
  }

  /**
   * Set a simulation parameter to a particular value
   *
   * @param parameter Name of parameter
   * @param value     Value as {@link Double}
   * @return <code>true</code> when the parameter was changed successfully,
   *         <code>false</code> otherwise
   * @throws UnableToStartSession when the session cannot be started
   */
  public boolean setValueAttribute(final String parameter, final double value)
      throws UnableToStartSpectreSession {

    return this.setValueAttribute(parameter,
        new SkillFlonum(new BigDecimal(value, MathContext.DECIMAL64)));
  }

  /**
   * Set a simulation parameter to a particular value
   *
   * @param parameter Name of parameter
   * @param value     Value as {@link Integer}
   * @return <code>true</code> when the parameter was changed successfully,
   *         <code>false</code> otherwise
   * @throws UnableToStartSession when the session cannot be started
   */
  public boolean setValueAttribute(final String parameter, final int value)
      throws UnableToStartSpectreSession {
    return this.setValueAttribute(parameter, new SkillFixnum(value));
  }

  /**
   * Set a simulation parameter to a particular value
   *
   * @param parameter Name of parameter
   * @param value     Value as {@link BigDecimal}
   * @return <code>true</code> when the parameter was changed successfully,
   *         <code>false</code> otherwise
   * @throws UnableToStartSession when the session cannot be started
   */
  public boolean setValueAttribute(final String parameter,
      final BigDecimal value) throws UnableToStartSpectreSession {
    return this.setValueAttribute(parameter, new SkillFlonum(value));
  }

  /**
   * Set a simulation parameter to a particular value
   *
   * @param parameter Name of parameter
   * @param value     Value as {@link Object}
   * @return <code>true</code> when the parameter was changed successfully,
   *         <code>false</code> otherwise
   * @throws UnableToStartSession when the session cannot be started
   */
  public boolean setValueAttribute(final String parameter, final Object value)
      throws UnableToStartSpectreSession {
    return this.setValueAttribute(parameter, new SkillString(value.toString()));
  }

  /**
   * Set a value attribute in the session
   * 
   * @param parameter Name of the parameter
   * @param value     value of the parameter
   * @return <code>true</code> when the parameter was set, <code>false</code>
   *         otherwise
   * @throws UnableToStartSpectreSession when the session did not start
   */
  private boolean setValueAttribute(final String parameter,
      final SkillDataobject value) throws UnableToStartSpectreSession {

    if (!this.session.isActive()) {
      this.start();
    }

    if (!this.parameterMapping.containsKey(parameter)) {
      if (!this.readParameterIdentififer(parameter)) {

        System.err.println(
            "Parameter=" + parameter + " is not defined in the netlist");
        return false;
      }
    }

    SkillCommand command = null;
    SkillDataobject returnValue = null;

    try {

      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_SET_ATTRIBUTE)
          .buildCommand(new EvaluableToSkill[] {
              new SkillString(this.parameterMapping.get(parameter)),
              new SkillString("value"), value });

      try {

        returnValue = this.session.evaluate(command, this.parentThread);

      } catch (final UnableToStartInteractiveSession e) {
      } catch (final EvaluationFailedException e) {
      } catch (final InvalidDataobjectReferenceExecption e) {
      }

      if ((returnValue != null) && returnValue.isTrue()) {
        this.parameterValues.put(parameter, value);
        return true;
      } else {
        return false;
      }

    } catch (final IncorrectSyntaxException e) {
      return false;
    }
  }

  /**
   * Get a real or integer numeric value from the session
   *
   * @param parameter Name of the parameter
   * @return value when parameter is available and a real or integer numeric,
   *         <code>null</code> otherwise
   * @throws UnableToStartSession when the session cannot be started
   */
  public BigDecimal getNumericValueAttribute(final String parameter)
      throws UnableToStartSpectreSession {
    final SkillDataobject obj = this.getValueAttribute(parameter);
    if (obj instanceof SkillFlonum) {
      return ((SkillFlonum) obj).getFlonum();
    } else if (obj instanceof SkillFixnum) {
      return new BigDecimal(((SkillFixnum) obj).getFixnum());
    } else {
      return null;
    }
  }

  /**
   * Get a value attribute
   * 
   * @param parameter Name of the parameter
   * @return value of the parameter
   * @throws UnableToStartSpectreSession when the session did not start
   */
  private SkillDataobject getValueAttribute(final String parameter)
      throws UnableToStartSpectreSession {

    if (!this.session.isActive()) {
      this.start();
    }

    if (!this.parameterMapping.containsKey(parameter)) {
      if (!this.readParameterIdentififer(parameter)) {

        System.err.println(
            "Parameter=" + parameter + " is not defined in the netlist");
        return SkillBoolean.getFalse();
      }
    }

    SkillCommand command = null;

    try {

      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_GET_ATTRIBUTE)
          .buildCommand(new EvaluableToSkill[] {
              new SkillString(this.parameterMapping.get(parameter)),
              new SkillString("value") });

      try {

        return this.session.evaluate(command, this.parentThread);

      } catch (final UnableToStartInteractiveSession e) {
      } catch (final EvaluationFailedException e) {
      } catch (final InvalidDataobjectReferenceExecption e) {
      }

    } catch (final IncorrectSyntaxException e) {
    }

    return SkillBoolean.getFalse();

  }

  /**
   * Get the identififer of a parameter
   * 
   * @param parameter Name of a parameter
   * @return <code>true</code> when the parameter is existing,
   *         <code>false</code> otherwise
   */
  private boolean readParameterIdentififer(final String parameter) {

    SkillCommand command = null;

    try {

      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_GET_PARAMETER)
          .buildCommand(new EvaluableToSkill[] {
              SpectreCommandTemplates
                  .getTemplate(SpectreCommandTemplates.SCL_GET_CIRCUT)
                  .buildCommand(new SkillString("")),
              new SkillString(parameter) });

    } catch (final IncorrectSyntaxException e) {
    }

    SkillDataobject returnValue = null;
    try {
      returnValue = this.session.evaluate(command, this.parentThread);
    } catch (final UnableToStartInteractiveSession e) {
    } catch (final EvaluationFailedException e) {
    } catch (final InvalidDataobjectReferenceExecption e) {
    }

    if ((returnValue != null) && returnValue.isTrue()) {
      final SkillString identifier = (SkillString) returnValue;
      this.parameterMapping.put(parameter, identifier.getString());
      return true;

    } else {
      return false;
    }
  }

  /**
   * Set the result directory for simulation. This function is triggered every
   * new simulation run
   * 
   * @return <code>true</code> when the result directory was set successfully,
   *         <code>false</code> otherwise
   */
  private boolean setResultDir() {

    SkillCommand command = null;

    try {

      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_SET_RES_DIR)
          .buildCommand(new SkillString("./"));

    } catch (final IncorrectSyntaxException e) {
      return false;
    }

    SkillDataobject returnValue;
    try {
      returnValue = this.session.evaluate(command, this.parentThread);
    } catch (final Exception e) {
      return false;
    }

    if ((returnValue != null) && returnValue.isTrue()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<NutmegPlot> simulate() throws UnableToStartSpectreSession {

    SkillCommand command = null;

    try {
      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_RUN)
          .buildCommand(new SkillString("all"));

      return this.simulate(command);
    } catch (final IncorrectSyntaxException e) {
      return new LinkedList<>();
    }
  }

  /**
   * Run a simulation while preventing some analyses to be executed
   *
   * @param analysesBlacklist analyses that are not executed
   * @return plots list of plots
   * @throws UnableToStartSession when the session cannot be started
   */
  public List<NutmegPlot> simulate(final Set<String> analysesBlacklist)
      throws UnableToStartSpectreSession {

    if (this.analysesMapping == null) {
      this.buildAnalysesMapping();
    }

    final List<String> identifiersToSimulate = new ArrayList<>();

    for (final String analyis : this.analyses) {

      if (!analysesBlacklist.contains(analyis)) {
        identifiersToSimulate.add(this.analysesMapping.get(analyis));
      }
    }

    final String[] identifiersToSimulateArray = new String[identifiersToSimulate
        .size()];

    for (int i = 0; i < identifiersToSimulateArray.length; i++) {
      identifiersToSimulateArray[i] = identifiersToSimulate.get(i);

    }

    try {

      final SkillCommand command = GenericSkillCommandTemplates
          .getTemplate(GenericSkillCommandTemplates.MAPCAR)
          .buildCommand(new EvaluableToSkill[] {
              new SkillSymbol(SpectreCommandTemplates.SCL_RUN_ANALYSIS),
              new SkillList(identifiersToSimulateArray) });

      return this.simulate(command);

    } catch (final IncorrectSyntaxException e) {
      return new LinkedList<>();
    }
  }

  private List<NutmegPlot> simulate(final SkillCommand command)
      throws UnableToStartSpectreSession {

    if (!this.session.isActive()) {
      this.start();
    }

    if (this.session.isActive()) {

      List<NutmegPlot> plots = new LinkedList<>();

      if (!this.setResultDir()) {
        System.err.println("Unable to set result dir");
        return plots;
      }

      SkillDataobject resultValue = null;

      try {
        resultValue = this.session.evaluate(command, this.parentThread);
      } catch (EvaluationFailedException
          | InvalidDataobjectReferenceExecption e) {
      } catch (final UnableToStartInteractiveSession e) {
        throw new UnableToStartSpectreSession(this.formatShellCommand(),
            this.workingDir, new File(this.workingDir, SpectreSession.LOG_FILENAME));
      }

      if (resultValue.isTrue()) {

        if (this.rawFile.exists()) {

          NutReader reader = null;

          if (this.resultFmt == RESULT_FMT.NUTASCII) {

            reader = NutReader.getNutasciiReader(this.rawFile.toString());

          } else if (this.resultFmt == RESULT_FMT.NUTBIN) {

            reader = NutReader.getNutbinReader(this.rawFile.toString());
          }

          plots = reader.read().parse().getPlots();

        } else {
          return this.simulate(command);
        }
      } else {
        System.err.println(resultValue.toSkill());
        throw new UnableToStartSpectreSession(this.formatShellCommand(),
            this.workingDir, new File(this.workingDir, SpectreSession.LOG_FILENAME));
      }

      if (this.rawFile.exists()) {
        this.rawFile.delete();
      }

      return plots;

    } else {
      return null;
    }
  }

  /**
   * Get the process identifier (PID) of the Spectre session
   *
   * @return pid when running, <code>-1</code> otherwise
   */
  public int getPid() {

    SkillCommand command;

    try {

      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_GET_PID).buildCommand();

    } catch (final IncorrectSyntaxException e) {
      return -1;
    }

    SkillDataobject returnValue;
    try {
      returnValue = this.session.evaluate(command, this.parentThread);
    } catch (final Exception e) {
      return -1;
    }

    if (returnValue instanceof SkillFixnum) {
      final SkillFixnum pid = (SkillFixnum) returnValue;
      return pid.getFixnum();
    } else {
      return -1;
    }
  }

  /**
   * Get all nets in the netlist
   *
   * @return set of all nets
   */
  public Set<String> getNets() {

    final Set<String> retval = new HashSet<>();

    SkillCommand command;

    try {
      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_LIST_NET).buildCommand();
    } catch (final IncorrectSyntaxException e) {
      return retval;
    }

    SkillDataobject returnValue = null;

    try {
      returnValue = this.session.evaluate(command, this.parentThread);
    } catch (final Exception e) {
    }

    SkillList nets;

    try {
      nets = (SkillList) returnValue;
    } catch (final Exception e) {
      return retval;
    }

    SkillString net;

    for (final SkillDataobject obj : nets) {
      net = (SkillString) obj;
      retval.add(net.getString());
    }

    return retval;
  }

  private void buildAnalysesMapping() {

    final List<String> analysesNames = this.getAnalyses();

    final String[] analysesNamesArray = new String[analysesNames.size()];
    this.analysesMapping = new HashMap<>();

    for (int i = 0; i < analysesNamesArray.length; i++) {
      analysesNamesArray[i] = analysesNames.get(i);
    }

    try {

      final SkillCommand cmd = GenericSkillCommandTemplates
          .getTemplate(GenericSkillCommandTemplates.MAPCAR)
          .buildCommand(new EvaluableToSkill[] {
              new SkillSymbol(SpectreCommandTemplates.SCL_GET_ANALYSIS),
              new SkillList(analysesNamesArray) });

      final SkillList identifiers = (SkillList) this.session.evaluate(cmd,
          this.parentThread);
      SkillString identifier;

      int i = 0;

      for (final SkillDataobject obj : identifiers) {
        identifier = (SkillString) obj;
        this.analysesMapping.put(analysesNamesArray[i], identifier.getString());
        i++;
      }

    } catch (final Error e) {
    } catch (final IncorrectSyntaxException e) {
    } catch (final UnableToStartInteractiveSession e) {
    } catch (final EvaluationFailedException e) {
    } catch (final InvalidDataobjectReferenceExecption e) {
    }
  }

  /**
   * Get a list of all analyses defined in the netlist.
   *
   * @return list of analyses
   */
  public List<String> getAnalyses() {

    if (this.analyses == null) {

      final List<String> retval = new LinkedList<>();

      SkillCommand command;

      try {

        command = SpectreCommandTemplates
            .getTemplate(SpectreCommandTemplates.SCL_LIST_ANALYSIS)
            .buildCommand();
      } catch (final IncorrectSyntaxException e) {
        return retval;
      }

      SkillDataobject returnValue = null;

      try {
        returnValue = this.session.evaluate(command, this.parentThread);
      } catch (final Exception e) {
      }

      SkillList analyses;

      try {
        analyses = (SkillList) returnValue;

        SkillList analysis;
        SkillString name;

        @SuppressWarnings("unused")
        SkillString type;

        for (final SkillDataobject obj : analyses) {

          analysis = (SkillList) obj;

          name = (SkillString) analysis.getByIndex(0);
          type = (SkillString) analysis.getByIndex(1);

          retval.add(name.getString());
        }
      } catch (final Exception e) {
        return retval;
      }

      this.analyses = retval;

      return retval;

    } else {
      return this.analyses;
    }
  }

  /**
   * Get a map of all instances defined in the netlist.The key identifies the
   * name of the instance and the value identifies the type.
   *
   * @return map of instances
   */
  public Map<String, String> getInstances() {

    final Map<String, String> retval = new HashMap<>();

    SkillCommand command;

    try {
      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_LIST_INSTANCE)
          .buildCommand();
    } catch (final IncorrectSyntaxException e) {
      return retval;
    }

    SkillDataobject returnValue = null;

    try {
      returnValue = this.session.evaluate(command, this.parentThread);
    } catch (final Exception e) {
    }

    SkillList analyses;

    try {
      analyses = (SkillList) returnValue;

      SkillList analysis;
      SkillString name;
      SkillString type;

      for (final SkillDataobject obj : analyses) {

        analysis = (SkillList) obj;

        name = (SkillString) analysis.getByIndex(0);
        type = (SkillString) analysis.getByIndex(1);

        retval.put(name.getString(), type.getString());
      }
    } catch (final Exception e) {
      return retval;
    }

    return retval;
  }

  /**
   * Stop the session
   * 
   * @return this;
   */
  public SpectreInteractiveSession stop() {
    this.session.stop();
    return this;
  }

  @Override
  protected void finalize() {
    this.stop();
  }

  /**
   * Get a map of plots from a list of plots. The key in the map corresponds to
   * the name of the plot/title of the analysis.
   *
   * @param plots list of plots
   * @return map of plots
   */
  public static Map<String, NutmegPlot> getMapOfPlots(
      final List<NutmegPlot> plots) {

    final Map<String, NutmegPlot> retval = new HashMap<>();

    for (final NutmegPlot nutmegPlot : plots) {
      retval.put(nutmegPlot.getPlotname(), nutmegPlot);
    }

    return retval;
  }

  /**
   * Get the path to a resource in the JAR
   *
   * @param fileName Name of the file
   * @param suffix   Suffix of the temporary file
   * @return reference to resource
   */
  public File getResourcePath(final String fileName, final String suffix) {
    return this.session.getResourcePath(fileName, suffix);
  }

  @Override
  @Deprecated
  public SpectreInteractiveSession simulateOnly()
      throws UnableToStartSpectreSession {
    System.err.println("Simulate-Only of Interactive Session not supported");
    return this;
  }

  @Override
  @Deprecated
  public List<NutmegPlot> readResults() {
    System.err
        .println("Results from an interactive session are read automatically");
    return new LinkedList<>();
  }
}