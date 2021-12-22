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
import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.reader.nutmeg.NutReader;
import edlab.eda.reader.nutmeg.NutmegPlot;

/**
 * Interactive Session
 */
public class SpectreInteractiveSession extends SpectreSession {

  private Map<String, String> parameterMapping = new HashMap<>();
  private Map<String, SkillDataobject> parameterValues = new HashMap<>();

  private List<String> analyses = null;
  private Map<String, String> analysesMapping;

  private SkillInteractiveSession session;
  private Thread parentThread;

  /**
   * Create a new Spectre session
   *
   * @param factory Factory that instantiated this session
   * @param name    Name of the session
   */
  SpectreInteractiveSession(SpectreFactory factory, String name) {

    super(factory, name);

    // this.parentThread = Thread.currentThread();
    this.parentThread = null;

    this.session = new SkillInteractiveSession(this.workingDir);
    this.session.setCommand(this.formatShellCommand());

    this.session.setTimeout(factory.getTimeoutDuration(),
        factory.getTimeoutTimeUnit());
  }

  /**
   * Set the parent thread of the session
   *
   * @param thread parent thread
   */
  public void setParentThread(Thread thread) {
    this.parentThread = thread;
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
  public void setNetlist(String netlist) {

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
      } catch (Exception e) {
      }
    }
  }

  @Override
  public void setNetlist(File netlist) throws IOException {

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
      } catch (Exception e) {

      }
    }
  }

  @Override
  public boolean addIncludeDirectory(File includeDirectory)
      throws FileNotFoundException {

    if (includeDirectory != null && includeDirectory.isDirectory()
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
  public boolean removeIncludeDirectory(File includeDirectory) {

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

    String cmd = this.factory.getCommand();

    if (this.mode == MODE.BIT64) {
      cmd += " -64";
    } else if (mode == MODE.BIT32) {
      cmd += " -32";
    }

    cmd += " +interactive";

    if (resultFmt == RESULT_FMT.NUTBIN) {
      cmd += " -format nutbin";
    } else if (resultFmt == RESULT_FMT.NUTASCII) {
      cmd += " -format nutascii";
    }

    if (this.noOfThreads > 1) {
      cmd += " +multithread=" + this.noOfThreads;
    }

    cmd += " -ahdllibdir ./" + AHDLLIB_DIRNAME;
    cmd += " =log " + LOG_FILENAME;

    for (File file : this.includeDirectories) {
      cmd += " -I" + file.getAbsolutePath();
    }

    cmd += " " + SpectreSession.getNetlistName();

    return cmd;
  }

  /**
   * Start {@link SkillInteractiveSession}
   *
   * @return <code>true</code> when the session is started successfully,
   *         <code>false</code> otherwise
   * @throws UnableToStartSession when the session cannot be started
   */
  public SpectreInteractiveSession start() throws UnableToStartSession {

    if (!this.writeNetlist()) {
      throw new UnableToStartSession(this.formatShellCommand(), this.workingDir,
          new File(workingDir, LOG_FILENAME));
    }

    try {

      this.session.setCommand(this.formatShellCommand());

      this.session.start(this.parentThread);
      this.analyses = null;
      this.parameterMapping = new HashMap<>();
      this.analysesMapping = null;

    } catch (Exception e) {
      throw new UnableToStartSession(this.formatShellCommand(), this.workingDir,
          new File(workingDir, LOG_FILENAME));
    }

    for (Entry<String, SkillDataobject> param : this.parameterValues
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
  public boolean setValueAttribute(String parameter, String value)
      throws UnableToStartSession {
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
  public boolean setValueAttribute(String parameter, double value)
      throws UnableToStartSession {

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
  public boolean setValueAttribute(String parameter, int value)
      throws UnableToStartSession {
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
  public boolean setValueAttribute(String parameter, BigDecimal value)
      throws UnableToStartSession {
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
  public boolean setValueAttribute(String parameter, Object value)
      throws UnableToStartSession {
    return this.setValueAttribute(parameter, new SkillString(value.toString()));
  }

  private boolean setValueAttribute(String parameter, SkillDataobject value)
      throws UnableToStartSession {

    if (!this.session.isActive()) {
      this.start();
    }

    if (!this.parameterMapping.containsKey(parameter)) {
      if (!readParameterIdentififer(parameter)) {

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

        returnValue = session.evaluate(command, this.parentThread);

      } catch (UnableToStartSession e) {
      } catch (EvaluationFailedException e) {
      } catch (InvalidDataobjectReferenceExecption e) {
      }

      if (returnValue != null && returnValue.isTrue()) {
        this.parameterValues.put(parameter, value);
        return true;
      } else {
        return false;
      }

    } catch (IncorrectSyntaxException e) {
      return false;
    }
  }

  /**
   * Get a numeric value from the session
   *
   * @param parameter Name of the parameter
   * @return value when parameter is valid, <code>null</code> otherwise
   * @throws UnableToStartSession when the session cannot be started
   */
  public BigDecimal getNumericValueAttribute(String parameter)
      throws UnableToStartSession {
    SkillDataobject obj = this.getValueAttribute(parameter);
    if (obj instanceof SkillFlonum) {
      return ((SkillFlonum) obj).getFlonum();
    } else {
      return null;
    }
  }

  private SkillDataobject getValueAttribute(String parameter)
      throws UnableToStartSession {

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

        return session.evaluate(command, this.parentThread);

      } catch (UnableToStartSession e) {
      } catch (EvaluationFailedException e) {
      } catch (InvalidDataobjectReferenceExecption e) {
      }

    } catch (IncorrectSyntaxException e) {
    }

    return SkillBoolean.getFalse();

  }

  private boolean readParameterIdentififer(String parameter) {

    SkillCommand command = null;

    try {

      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_GET_PARAMETER)
          .buildCommand(new EvaluableToSkill[] {
              SpectreCommandTemplates
                  .getTemplate(SpectreCommandTemplates.SCL_GET_CIRCUT)
                  .buildCommand(new SkillString("")),
              new SkillString(parameter) });

    } catch (IncorrectSyntaxException e) {
    }

    SkillDataobject returnValue = null;
    try {
      returnValue = session.evaluate(command, this.parentThread);
    } catch (UnableToStartSession e) {
    } catch (EvaluationFailedException e) {
    } catch (InvalidDataobjectReferenceExecption e) {
    }

    if (returnValue != null && returnValue.isTrue()) {
      SkillString identifier = (SkillString) returnValue;
      this.parameterMapping.put(parameter, identifier.getString());
      return true;

    } else {
      return false;
    }
  }

  private boolean setResultDir() {

    SkillCommand command = null;

    try {

      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_SET_RES_DIR)
          .buildCommand(new SkillString("./"));

    } catch (IncorrectSyntaxException e) {
      return false;
    }

    SkillDataobject returnValue;
    try {
      returnValue = session.evaluate(command, this.parentThread);
    } catch (Exception e) {
      return false;
    }

    if (returnValue != null && returnValue.isTrue()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<NutmegPlot> simulate() throws UnableToStartSession {

    SkillCommand command = null;

    try {
      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_RUN)
          .buildCommand(new SkillString("all"));

      return this.simulate(command);
    } catch (IncorrectSyntaxException e) {
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
  public List<NutmegPlot> simulate(Set<String> analysesBlacklist)
      throws UnableToStartSession {

    if (this.analysesMapping == null) {
      this.buildAnalysesMapping();
    }

    List<String> identifiersToSimulate = new ArrayList<>();

    for (String analyis : this.analyses) {

      if (!analysesBlacklist.contains(analyis)) {
        identifiersToSimulate.add(this.analysesMapping.get(analyis));
      }
    }

    String[] identifiersToSimulateArray = new String[identifiersToSimulate
        .size()];

    for (int i = 0; i < identifiersToSimulateArray.length; i++) {
      identifiersToSimulateArray[i] = identifiersToSimulate.get(i);

    }

    try {

      SkillCommand command = GenericSkillCommandTemplates
          .getTemplate(GenericSkillCommandTemplates.MAPCAR)
          .buildCommand(new EvaluableToSkill[] {
              new SkillSymbol(SpectreCommandTemplates.SCL_RUN_ANALYSIS),
              new SkillList(identifiersToSimulateArray) });

      return this.simulate(command);

    } catch (IncorrectSyntaxException e) {
      return new LinkedList<>();
    }
  }

  private List<NutmegPlot> simulate(SkillCommand command)
      throws UnableToStartSession {

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
      }

      if (resultValue.isTrue()) {

        if (rawFile.exists()) {

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
        throw new UnableToStartSession(this.formatShellCommand(),
            this.workingDir, new File(workingDir, LOG_FILENAME));
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

    } catch (IncorrectSyntaxException e) {
      return -1;
    }

    SkillDataobject returnValue;
    try {
      returnValue = this.session.evaluate(command, this.parentThread);
    } catch (Exception e) {
      return -1;
    }

    if (returnValue instanceof SkillFixnum) {
      SkillFixnum pid = (SkillFixnum) returnValue;
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

    Set<String> retval = new HashSet<>();

    SkillCommand command;

    try {
      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_LIST_NET).buildCommand();
    } catch (IncorrectSyntaxException e) {
      return retval;
    }

    SkillDataobject returnValue = null;

    try {
      returnValue = this.session.evaluate(command, this.parentThread);
    } catch (Exception e) {
    }

    SkillList nets;

    try {
      nets = (SkillList) returnValue;
    } catch (Exception e) {
      return retval;
    }

    SkillString net;

    for (SkillDataobject obj : nets) {
      net = (SkillString) obj;
      retval.add(net.getString());
    }

    return retval;
  }

  private void buildAnalysesMapping() {

    List<String> analysesNames = this.getAnalyses();

    String[] analysesNamesArray = new String[analysesNames.size()];
    this.analysesMapping = new HashMap<>();

    for (int i = 0; i < analysesNamesArray.length; i++) {
      analysesNamesArray[i] = analysesNames.get(i);
    }

    try {

      SkillCommand cmd = GenericSkillCommandTemplates
          .getTemplate(GenericSkillCommandTemplates.MAPCAR)
          .buildCommand(new EvaluableToSkill[] {
              new SkillSymbol(SpectreCommandTemplates.SCL_GET_ANALYSIS),
              new SkillList(analysesNamesArray) });

      SkillList identifiers = (SkillList) this.session.evaluate(cmd,
          this.parentThread);
      SkillString identifier;

      int i = 0;

      for (SkillDataobject obj : identifiers) {
        identifier = (SkillString) obj;
        this.analysesMapping.put(analysesNamesArray[i], identifier.getString());
        i++;
      }

    } catch (Error e) {
    } catch (IncorrectSyntaxException e) {
    } catch (UnableToStartSession e) {
    } catch (EvaluationFailedException e) {
    } catch (InvalidDataobjectReferenceExecption e) {
    }
  }

  /**
   * Get a list of all analyses defined in the netlist.
   *
   * @return list of analyses
   */
  public List<String> getAnalyses() {

    if (this.analyses == null) {

      List<String> retval = new LinkedList<>();

      SkillCommand command;

      try {

        command = SpectreCommandTemplates
            .getTemplate(SpectreCommandTemplates.SCL_LIST_ANALYSIS)
            .buildCommand();
      } catch (IncorrectSyntaxException e) {
        return retval;
      }

      SkillDataobject returnValue = null;

      try {
        returnValue = this.session.evaluate(command, this.parentThread);
      } catch (Exception e) {
      }

      SkillList analyses;

      try {
        analyses = (SkillList) returnValue;

        SkillList analysis;
        SkillString name;

        @SuppressWarnings("unused")
        SkillString type;

        for (SkillDataobject obj : analyses) {

          analysis = (SkillList) obj;

          name = (SkillString) analysis.getByIndex(0);
          type = (SkillString) analysis.getByIndex(1);

          retval.add(name.getString());
        }
      } catch (Exception e) {
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

    Map<String, String> retval = new HashMap<>();

    SkillCommand command;

    try {
      command = SpectreCommandTemplates
          .getTemplate(SpectreCommandTemplates.SCL_LIST_INSTANCE)
          .buildCommand();
    } catch (IncorrectSyntaxException e) {
      return retval;
    }

    SkillDataobject returnValue = null;

    try {
      returnValue = this.session.evaluate(command, this.parentThread);
    } catch (Exception e) {
    }

    SkillList analyses;

    try {
      analyses = (SkillList) returnValue;

      SkillList analysis;
      SkillString name;
      SkillString type;

      for (SkillDataobject obj : analyses) {

        analysis = (SkillList) obj;

        name = (SkillString) analysis.getByIndex(0);
        type = (SkillString) analysis.getByIndex(1);

        retval.put(name.getString(), type.getString());
      }
    } catch (Exception e) {
      return retval;
    }

    return retval;
  }

  /**
   * Stop the session
   */
  public void stop() {
    this.session.stop();
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
  public static Map<String, NutmegPlot> getMapOfPlots(List<NutmegPlot> plots) {

    Map<String, NutmegPlot> retval = new HashMap<>();

    for (NutmegPlot nutmegPlot : plots) {
      retval.put(nutmegPlot.getPlotname(), nutmegPlot);
    }

    return retval;
  }

  /**
   * Get the path to a resource in the JAR
   *
   * @param fileName Name of the file
   * @param suffix   Suffix of the temporaray file
   * @return reference to resource
   */
  public File getResourcePath(String fileName, String suffix) {
    return this.session.getResourcePath(fileName, suffix);
  }
}