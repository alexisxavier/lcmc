/*
 * This file is part of LCMC written by Rasto Levrinc.
 *
 * Copyright (C) 2014, Rastislav Levrinc.
 *
 * The LCMC is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * The LCMC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LCMC; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package lcmc;

import lcmc.model.*;
import lcmc.robotest.RoboTest;
import lcmc.robotest.Test;
import lcmc.utilities.Logger;
import lcmc.utilities.LoggerFactory;
import lcmc.utilities.Tools;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ArgumentParser {
    private static final Logger LOG = LoggerFactory.getLogger(ArgumentParser.class);

    private static final String HELP_OP = "help";
    private static final String VERSION_OP = "version";
    private static final String NOLRM_OP = "nolrm";
    private static final String AUTO_OP = "auto";
    private static final String PCMKTEST_OP = "pcmktest";
    private static final String DRBDTEST_OP = "drbdtest";
    private static final String VMTEST_OP = "vmtest";
    private static final String GUITEST_OP = "guitest";
    private static final String RO_OP = "ro";
    private static final String OP_OP = "op";
    private static final String ADMIN_OP = "admin";
    private static final String OP_MODE_OP = "op-mode";
    private static final String NO_UPGRADE_CHECK_OP = "no-upgrade-check";
    /** The --no-plugin-check option. DEPRECATED, doesn't do anything */
    private static final String NO_PLUGIN_CHECK_OP = "no-plugin-check";
    private static final String TIGHTVNC_OP = "tightvnc";
    private static final String ULTRAVNC_OP = "ultravnc";
    private static final String REALVNC_OP = "realvnc";
    private static final String BIGDRBDCONF_OP = "big-drbd-conf";
    private static final String STAGING_DRBD_OP = "staging-drbd";
    private static final String STAGING_PACEMAKER_OP = "staging-pacemaker";
    private static final String VNC_PORT_OFFSET_OP = "vnc-port-offset";
    private static final String SLOW_OP = "slow";
    private static final String RESTORE_MOUSE_OP = "restore-mouse";
    private static final String KEEP_HELPER_OP = "keep-helper";
    private static final String SCALE_OP = "scale";
    private static final String ID_DSA_OP = "id-dsa";
    private static final String ID_RSA_OP = "id-rsa";
    private static final String KNOWN_HOSTS_OP = "known-hosts";
    private static final String OUT_OP = "out";
    private static final String DEBUG_OP = "debug";
    private static final String CLUSTER_OP = "cluster";
    private static final String HOST_OP = "host";
    private static final String USER_OP = "user";
    private static final String SUDO_OP = "sudo";
    private static final String PORT_OP = "port";
    private static final String ADVANCED_OP = "advanced";
    private static final String ONE_HOST_CLUSTER_OP = "one-host-cluster";
    private static final String NO_PASSPHRASE_OP = "no-passphrase";
    /** The --embed. Embed in the browser option. */
    private static final String EMBED_OP = "embed";

    /** The --no-embed. Don't embed in the browser option. */
    private static final String NO_EMBED_OP = "no-embed";
    /** The --cmd-log. /var/log/lcmc.log on the servers. */
    private static final String CMD_LOG_OP = "cmd-log";
    private static final String CHECK_SWING_OP = "check-swing";
    @Autowired
    private UserConfig userConfig;
    @Autowired
    private RoboTest roboTest;

    public void parseOptionsAndReturnAutoArguments(String[] args) {
        final Options options = new Options();

        options.addOption("h", HELP_OP, false, "print this help");
        options.addOption(null, KEEP_HELPER_OP, false, "do not overwrite the lcmc-gui-helper program");
        options.addOption(null, RO_OP, false, "read only mode");
        options.addOption(null, OP_OP, false, "operator mode");
        options.addOption(null, ADMIN_OP, false, "administrator mode");
        options.addOption(null, OP_MODE_OP, true, "operating mode. <arg> can be:\n"
                                                  + "ro - read only\n"
                                                  + "op - operator\n"
                                                  + "admin - administrator");
        options.addOption(null, NOLRM_OP, false, "do not show removed resources from LRM.");
        options.addOption("v", VERSION_OP, false, "print version");
        options.addOption(null, AUTO_OP, true, "ADVANCED USE: for testing");
        options.addOption(null, PCMKTEST_OP, true, "ADVANCED USE: for testing");
        options.addOption(null, DRBDTEST_OP, true, "ADVANCED USE: for testing");
        options.addOption(null, VMTEST_OP, true, "ADVANCED USE: for testing");
        options.addOption(null, GUITEST_OP, true, "ADVANCED USE: for testing");
        options.addOption(null, NO_UPGRADE_CHECK_OP, false, "disable upgrade check");
        options.addOption(null, NO_PLUGIN_CHECK_OP, false, "disable plugin check, DEPRECATED: there are no plugins");
        options.addOption(null, TIGHTVNC_OP, false, "enable tight vnc viewer");
        options.addOption(null, ULTRAVNC_OP, false, "enable ultra vnc viewer");
        options.addOption(null, REALVNC_OP, false, "enable real vnc viewer");
        options.addOption(null, BIGDRBDCONF_OP, false, "create one big drbd.conf, instead of many"
                + " files in drbd.d/ directory");
        options.addOption(null, STAGING_DRBD_OP, false, "enable more DRBD installation options");
        options.addOption(null, STAGING_PACEMAKER_OP, false, "enable more Pacemaker installation options");
        options.addOption(null, VNC_PORT_OFFSET_OP, true, "offset for port forwarding");
        options.addOption(null, SLOW_OP, false, "specify this if you have slow computer");
        options.addOption(null, RESTORE_MOUSE_OP, false, "ADVANCED USE: for testing");
        options.addOption(null, SCALE_OP, true, "scale fonts and sizes of elements in percent (100)");
        options.addOption(null, ID_DSA_OP, true, "location of id_dsa file ($HOME/.ssh/id_dsa)");
        options.addOption(null, ID_RSA_OP, true, "location of id_rsa file ($HOME/.ssh/id_rsa)");
        options.addOption(null, KNOWN_HOSTS_OP, true, "location of known_hosts file ($HOME/.ssh/known_hosts)");
        options.addOption(null, OUT_OP, true, "where to redirect the standard out");
        options.addOption(null, DEBUG_OP, true, "debug level, 0 - none, 3 - all");
        options.addOption("c", CLUSTER_OP, true, "define a cluster");
        final Option hostOp = new Option("h", HOST_OP, true, "define a cluster, used with --cluster option");
        hostOp.setArgs(10000);
        options.addOption(hostOp);
        options.addOption(null, SUDO_OP, false, "whether to use sudo, used with --cluster option");
        options.addOption(null, USER_OP, true, "user to use with sudo, used with --cluster option");
        options.addOption(null, PORT_OP, true, "ssh port, used with --cluster option");
        options.addOption(null, ADVANCED_OP, false, "start in an advanced mode");
        options.addOption(null, ONE_HOST_CLUSTER_OP, false, "allow one host cluster");
        options.addOption(null, NO_PASSPHRASE_OP, false, "try no passphrase first");
        options.addOption(null, EMBED_OP, false, "embed applet in the browser");
        options.addOption(null, NO_EMBED_OP, false, "don't embed applet in the browser");
        options.addOption(null, CMD_LOG_OP, false, "Log executed commands to the lcmc.log on the servers");
        options.addOption(null, CHECK_SWING_OP, false, "ADVANCED USE: for testing");
        final CommandLineParser parser = new PosixParser();
        String autoArgs = null;
        try {
            final CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption(OUT_OP)) {
                final String out = cmd.getOptionValue(OUT_OP);
                if (out != null) {
                    try {
                        System.setOut(new PrintStream(new FileOutputStream(out)));
                    } catch (final FileNotFoundException e) {
                        System.exit(2);
                    }
                }
            }
            if (cmd.hasOption(DEBUG_OP)) {
                final String level = cmd.getOptionValue(DEBUG_OP);
                if (level != null && lcmc.utilities.Tools.isNumber(level)) {
                    LoggerFactory.setDebugLevel(Integer.parseInt(level));
                } else {
                    throw new ParseException("cannot parse debug level: " + level);
                }
            }
            boolean tightvnc = cmd.hasOption(TIGHTVNC_OP);
            boolean ultravnc = cmd.hasOption(ULTRAVNC_OP);
            final boolean realvnc = cmd.hasOption(REALVNC_OP);
            if (!tightvnc && !ultravnc && !realvnc) {
                if (lcmc.utilities.Tools.isLinux()) {
                    tightvnc = true;
                } else {
                    tightvnc = true;
                    ultravnc = true;
                }
            }
            final boolean advanced = cmd.hasOption(ADVANCED_OP);
            lcmc.utilities.Tools.getApplication().setAdvancedMode(advanced);
            lcmc.utilities.Tools.getApplication().setUseTightvnc(tightvnc);
            lcmc.utilities.Tools.getApplication().setUseUltravnc(ultravnc);
            lcmc.utilities.Tools.getApplication().setUseRealvnc(realvnc);

            lcmc.utilities.Tools.getApplication().setUpgradeCheckEnabled(!cmd.hasOption(NO_UPGRADE_CHECK_OP));
            lcmc.utilities.Tools.getApplication().setBigDRBDConf(cmd.hasOption(BIGDRBDCONF_OP));
            lcmc.utilities.Tools.getApplication().setStagingDrbd(cmd.hasOption(STAGING_DRBD_OP));
            lcmc.utilities.Tools.getApplication().setStagingPacemaker(cmd.hasOption(STAGING_PACEMAKER_OP));
            lcmc.utilities.Tools.getApplication().setHideLRM(cmd.hasOption(NOLRM_OP));
            lcmc.utilities.Tools.getApplication().setKeepHelper(cmd.hasOption(KEEP_HELPER_OP));
            lcmc.utilities.Tools.getApplication().setOneHostCluster(cmd.hasOption(ONE_HOST_CLUSTER_OP));
            lcmc.utilities.Tools.getApplication().setNoPassphrase(cmd.hasOption(NO_PASSPHRASE_OP));
            if (cmd.hasOption(EMBED_OP)) {
                lcmc.utilities.Tools.getApplication().setEmbedApplet(true);
            }
            if (cmd.hasOption(NO_EMBED_OP)) {
                lcmc.utilities.Tools.getApplication().setEmbedApplet(false);
            }
            if (cmd.hasOption(CMD_LOG_OP)) {
                lcmc.utilities.Tools.getApplication().setCmdLog(true);
            }
            if (cmd.hasOption(CHECK_SWING_OP)) {
                lcmc.utilities.Tools.getApplication().setCheckSwing(true);
            }
            final String pwd = System.getProperty("user.home");
            final String scaleOp = cmd.getOptionValue(SCALE_OP, "100");
            try {
                final int scale = Integer.parseInt(scaleOp);
                lcmc.utilities.Tools.getApplication().setScale(scale);
                lcmc.utilities.Tools.resizeFonts(scale);
            } catch (final NumberFormatException e) {
                LOG.appWarning("initApp: cannot parse scale: " + scaleOp);
            }

            final String idDsaPath = cmd.getOptionValue(ID_DSA_OP, pwd + "/.ssh/id_dsa");
            final String idRsaPath = cmd.getOptionValue(ID_RSA_OP, pwd + "/.ssh/id_rsa");
            final String knownHostsPath = cmd.getOptionValue(KNOWN_HOSTS_OP, pwd + "/.ssh/known_hosts");
            lcmc.utilities.Tools.getApplication().setIdDSAPath(idDsaPath);
            lcmc.utilities.Tools.getApplication().setIdRSAPath(idRsaPath);
            lcmc.utilities.Tools.getApplication().setKnownHostPath(knownHostsPath);

            final String opMode = cmd.getOptionValue(OP_MODE_OP);
            autoArgs = cmd.getOptionValue(AUTO_OP);
            if (cmd.hasOption(HELP_OP)) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar LCMC.jar [OPTIONS]", options);
                System.exit(0);
            }
            if (cmd.hasOption(VERSION_OP)) {
                System.out.println("LINUX CLUSTER MANAGEMENT CONSOLE " + lcmc.utilities.Tools.getRelease() + " by Rasto Levrinc");
                System.exit(0);
            }
            if (cmd.hasOption("ro") || "ro".equals(opMode)) {
                lcmc.utilities.Tools.getApplication().setAccessType(Application.AccessType.RO);
                lcmc.utilities.Tools.getApplication().setMaxAccessType(Application.AccessType.RO);
            } else if (cmd.hasOption("op") || "op".equals(opMode)) {
                lcmc.utilities.Tools.getApplication().setAccessType(Application.AccessType.OP);
                lcmc.utilities.Tools.getApplication().setMaxAccessType(Application.AccessType.OP);
            } else if (cmd.hasOption("admin") || "admin".equals(opMode)) {
                lcmc.utilities.Tools.getApplication().setAccessType(Application.AccessType.ADMIN);
                lcmc.utilities.Tools.getApplication().setMaxAccessType(Application.AccessType.ADMIN);
            } else if (opMode != null) {
                LOG.appWarning("initApp: unknown operating mode: " + opMode);
            }
            float fps = Application.DEFAULT_ANIM_FPS;
            if (cmd.hasOption(SLOW_OP)) {
                fps /= 2;
            }
            if (cmd.hasOption(RESTORE_MOUSE_OP)) {
                /* restore mouse if it is stuck in pressed state, during
                * robot tests. */
                roboTest.restoreMouse();
            }
            final String vncPortOffsetString = cmd.getOptionValue(VNC_PORT_OFFSET_OP);
            if (vncPortOffsetString != null && lcmc.utilities.Tools.isNumber(vncPortOffsetString)) {
                lcmc.utilities.Tools.getApplication().setVncPortOffset(Integer.parseInt(vncPortOffsetString));
            }
            lcmc.utilities.Tools.getApplication().setAnimFPS(fps);
            if (cmd.hasOption(CLUSTER_OP) || cmd.hasOption(HOST_OP)) {
                parseClusterOptionsAndCreateClusterButton(cmd);
            }
        } catch (final ParseException exp) {
            System.out.println("ERROR: " + exp.getMessage());
            System.exit(1);
        }
        LOG.debug1("initApp: max mem: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + 'm');
        if (autoArgs != null) {
            parseAutoArgs(autoArgs);
        }
    }

    public void parseClusterOptionsAndCreateClusterButton(final CommandLine cmd) throws ParseException {
        String clusterName = null;
        List<HostOptions> hostsOptions = null;
        final Map<String, List<HostOptions>> clusters = new LinkedHashMap<String, List<HostOptions>>();
        for (final Option option : cmd.getOptions()) {
            final String op = option.getLongOpt();
            if (CLUSTER_OP.equals(op)) {
                clusterName = option.getValue();
                if (clusterName == null) {
                    throw new ParseException("could not parse " + CLUSTER_OP + " option");

                }
                clusters.put(clusterName, new ArrayList<HostOptions>());
            } else if (HOST_OP.equals(op)) {
                final String[] hostNames = option.getValues();
                if (clusterName == null) {
                    clusterName = "default";
                    clusters.put(clusterName, new ArrayList<HostOptions>());
                }
                if (hostNames == null) {
                    throw new ParseException("could not parse " + HOST_OP + " option");
                }
                hostsOptions = new ArrayList<HostOptions>();
                for (final String hostNameEntered : hostNames) {
                    final String hostName;
                    String port = null;
                    if (hostNameEntered.indexOf(':') > 0) {
                        final String[] he = hostNameEntered.split(":");
                        hostName = he[0];
                        port = he[1];
                        if (port != null && port.isEmpty() || !lcmc.utilities.Tools.isNumber(port)) {
                            throw new ParseException("could not parse " + HOST_OP + " option");
                        }
                    } else {
                        hostName = hostNameEntered;
                    }
                    final HostOptions ho = new HostOptions(hostName);
                    if (port != null) {
                        ho.setPort(port);
                    }
                    hostsOptions.add(ho);
                    clusters.get(clusterName).add(ho);
                }
            } else if (SUDO_OP.equals(op)) {
                if (hostsOptions == null) {
                    throw new ParseException(SUDO_OP + " must be defined after " + HOST_OP);
                }
                for (final HostOptions ho : hostsOptions) {
                    ho.setUseSudo(true);
                }
            } else if (USER_OP.equals(op)) {
                if (hostsOptions == null) {
                    throw new ParseException(USER_OP + " must be defined after " + HOST_OP);
                }
                final String userName = option.getValue();
                if (userName == null) {
                    throw new ParseException("could not parse " + USER_OP + " option");
                }
                for (final HostOptions ho : hostsOptions) {
                    ho.setLoginUser(userName);
                }
            } else if (PORT_OP.equals(op)) {
                if (hostsOptions == null) {
                    throw new ParseException(PORT_OP + " must be defined after " + HOST_OP);
                }
                final String port = option.getValue();
                if (port == null) {
                    throw new ParseException("could not parse " + PORT_OP + " option");
                }
                for (final HostOptions ho : hostsOptions) {
                    ho.setPort(port);
                }
            } else if (PCMKTEST_OP.equals(op)) {
                final String index = option.getValue();
                if (index != null && !index.isEmpty()) {
                    lcmc.utilities.Tools.getApplication().setAutoTest(new Test(RoboTest.Type.PCMK, index.charAt(0)));
                }
            } else if (DRBDTEST_OP.equals(op)) {
                final String index = option.getValue();
                if (index != null && !index.isEmpty()) {
                    lcmc.utilities.Tools.getApplication().setAutoTest(new Test(RoboTest.Type.DRBD, index.charAt(0)));
                }
            } else if (VMTEST_OP.equals(op)) {
                final String index = option.getValue();
                if (index != null && !index.isEmpty()) {
                    lcmc.utilities.Tools.getApplication().setAutoTest(new Test(RoboTest.Type.VM, index.charAt(0)));
                }
            } else if (GUITEST_OP.equals(op)) {
                final String index = option.getValue();
                if (index != null && !index.isEmpty()) {
                    lcmc.utilities.Tools.getApplication().setAutoTest(new Test(RoboTest.Type.GUI, index.charAt(0)));
                }
            }
        }
        for (final Map.Entry<String, List<HostOptions>> clusterEntry : clusters.entrySet()) {
            final List<HostOptions> hostOptions = clusterEntry.getValue();
            if (hostOptions.size() < 1 || (hostOptions.size() == 1 && !lcmc.utilities.Tools.getApplication().isOneHostCluster())) {
                throw new ParseException("not enough hosts for cluster: " + clusterEntry.getKey());
            }
        }
        final String failedHost = setUserConfigFromOptions(clusters);
        if (failedHost != null) {
            LOG.appWarning("parseClusterOptions: could not resolve host \"" + failedHost + "\" skipping");
        }
    }

    /**
     * Parses arguments from --auto command line option, it makes some
     * automatical gui actions, that help to test the gui and can find some
     * other uses later.
     * To find out which options are available, you'd have to grep for
     * getAutoOptionHost and getAutoOptionCluster
     */
    void parseAutoArgs(final String line) {
        if (line == null) {
            return;
        }
        final String[] args = line.split(",");
        String host = null;
        String cluster = null;
        boolean global = false;
        for (final String arg : args) {
            final String[] pair = arg.split(":");
            if (pair == null || pair.length != 2) {
                LOG.appWarning("parseAutoArgs: cannot parse: " + line);
                return;
            }
            final String option = pair[0];
            final String value = pair[1];
            if ("host".equals(option)) {
                cluster = null;
                host = value;
                lcmc.utilities.Tools.getApplication().addAutoHost(host);
                continue;
            } else if ("cluster".equals(option)) {
                host = null;
                cluster = value;
                lcmc.utilities.Tools.getApplication().addAutoCluster(cluster);
                continue;
            } else if ("global".equals(option)) {
                host = null;
                cluster = null;
                global = true;
                continue;
            }
            if (host != null) {
                lcmc.utilities.Tools.getApplication().addAutoOption(host, option, value);
            } else if (cluster != null) {
                lcmc.utilities.Tools.getApplication().addAutoOption(cluster, option, value);
            } else if (global) {
                Tools.getApplication().addAutoOption("global", option, value);
            } else {
                LOG.appWarning("parseAutoArgs: cannot parse: " + line);
                return;
            }
        }
    }

    /** Sets user config from command line options returns host, for which dns lookup failed. */
    private String setUserConfigFromOptions(final Map<String, List<HostOptions>> clusters) {
        final Map<String, List<Host>> hostMap = new LinkedHashMap<String, List<Host>>();
        for (final String clusterName : clusters.keySet()) {
            for (final HostOptions hostOptions : clusters.get(clusterName)) {
                final String hostnameEntered = hostOptions.getHost();
                InetAddress[] addresses = null;
                try {
                    addresses = InetAddress.getAllByName(hostnameEntered);
                } catch (final UnknownHostException e) {
                }
                String ip = null;
                if (addresses != null) {
                    if (addresses.length == 0) {
                        LOG.debug("setUserConfigFromOptions: lookup failed");
                        /* lookup failed */
                    } else {
                        ip = addresses[0].getHostAddress();
                    }
                }
                if (ip == null) {
                    return hostnameEntered;
                }
                userConfig.setHost(hostMap,
                        hostOptions.getLoginUser(),
                        hostnameEntered,
                        ip,
                        hostOptions.getPort(),
                        null,
                        hostOptions.getUseSudo(),
                        false);
            }
        }
        for (final String clusterName : clusters.keySet()) {
            final Cluster cluster = new Cluster();
            cluster.setName(clusterName);
            cluster.setSavable(false);
            Tools.getApplication().addClusterToClusters(cluster);
            for (final HostOptions ho : clusters.get(clusterName)) {
                userConfig.setHostCluster(hostMap, cluster, ho.getHost(), !UserConfig.PROXY_HOST);
            }
        }
        return null;
    }

}
