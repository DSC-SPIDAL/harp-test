---
title: Quick Start Guide
description: Run a single-node Harp on your laptop
aliases:
  - /docs/install.html
---

This instruction is only available for:

* Mac OS X
* Ubuntu

If you are using windows, we suggest you to install an Ubuntu system on a virtualization software (e.g. VirtualBox) with at least 4GB memory in it.

## Step 1 --- Install hadoop-2.6.0

First of all, make sure your computer can use `ssh` to access `localhost` and install `Java` as well.

Download and extract the hadoop-2.6.0 binary into your machine. These are available at [hadoop-2.6.0.tar.gz](https://dist.apache.org/repos/dist/release/hadoop/common/hadoop-2.6.0/hadoop-2.6.0.tar.gz).

Then set the environment variables in `~/.bashrc`.

```bash
export JAVA_HOME=<where Java locates>
#e.g. ~/jdk1.8.0_91
export HADOOP_HOME=<where hadoop-2.6.0 locates>
#e.g. ~/hadoop-2.6.0
export YARN_HOME=$HADOOP_HOME
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
export PATH=$HADOOP_HOME/bin:$JAVA_HOME/bin:$PATH
```
Now run

```bash
$ source ~/.bashrc
```
in order to make sure the changes are applied.

Check if you can successfully run Hadoop command and get the following output.

```bash
$ hadoop
Usage: hadoop [--config confdir] COMMAND
       where COMMAND is one of:
  fs                   run a generic filesystem user client
  version              print the version
  jar <jar>            run a jar file
  checknative [-a|-h]  check native hadoop and compression libraries availability
  distcp <srcurl> <desturl> copy file or directories recursively
  archive -archiveName NAME -p <parent path> <src>* <dest> create a hadoop archive
  classpath            prints the class path needed to get the
  credential           interact with credential providers
                       Hadoop jar and the required libraries
  daemonlog            get/set the log level for each daemon
  trace                view and modify Hadoop tracing settings
 or
  CLASSNAME            run the class named CLASSNAME

Most commands print help when invoked w/o parameters.
```

Modify the following files in Apache Hadoop distribution.

`$HADOOP_HOME/etc/hadoop/core-site.xml`:

```html
<configuration>
  <property>
    <name>fs.default.name</name>
    <value>hdfs://localhost:9010</value>
  </property>
  <property>
    <name>hadoop.tmp.dir</name>
    <value>/tmp/hadoop-${user.name}</value>
    <description>A base for other temporary directories.</description>
  </property>
</configuration>
```

`$HADOOP_HOME/etc/hadoop/hdfs-site.xml`:
```html
<configuration>
  <property>
    <name>dfs.replication</name>
    <value>1</value>
  </property>
  <property>
    <name>dfs.namenode.http-address</name>
    <value>localhost:50070</value>
  </property>
  <property>
    <name>dfs.namenode.secondary.http-address</name>
    <value>localhost:50190</value>
  </property>
</configuration>
```

`$HADOOP_HOME/etc/hadoop/mapred-site.xml`:

You will be creating this file. It doesn’t exist in the original package.
```html
<configuration>
  <property>
    <name>mapreduce.framework.name</name>
    <value>yarn</value>
  </property>
  <property>
    <name>yarn.app.mapreduce.am.resource.mb</name>
    <value>512</value>
  </property>
  <property>
    <name>yarn.app.mapreduce.am.command-opts</name>
    <value>-Xmx256m -Xms256m</value>
  </property>
</configuration>
```

`$HADOOP_HOME/etc/hadoop/yarn-site.xml`:
```html
<configuration>
  <property>
    <name>yarn.resourcemanager.hostname</name>
    <value>localhost</value>
  </property>
  <property>
    <name>yarn.resourcemanager.address</name>
    <value>localhost:8132</value>
  </property>
  <property>
    <name>yarn.resourcemanager.scheduler.address</name>
    <value>localhost:8130</value>
  </property>
  <property>
    <name>yarn.resourcemanager.resource-tracker.address</name>
    <value>localhost:8131</value>
  </property>
  <property>
    <name>yarn.resourcemanager.admin.address</name>
    <value>localhost:8133</value>
  </property>
  <property>
    <name>yarn.resourcemanager.webapp.address</name>
    <value>localhost:8080</value>
  </property>
  <property>
    <name>yarn.nodemanager.aux-services</name>
    <value>mapreduce_shuffle</value>
  </property>
  <property>
    <name>yarn.nodemanager.resource.memory-mb</name>
    <value>4096</value>
  </property>
  <property>
    <description>Whether virtual memory limits will be enforced for containers.</description>
    <name>yarn.nodemanager.vmem-check-enabled</name>
    <value>false</value>
  </property>
  <property>
    <name>yarn.scheduler.minimum-allocation-mb</name>
    <value>512</value>
  </property>
  <property>
    <name>yarn.scheduler.maximum-allocation-mb</name>
    <value>2048</value>
  </property>
</configuration>
```

Next we format the file system and you should be able to see it exits with status 0.
```bash
$ hdfs namenode -format
...
xx/xx/xx xx:xx:xx INFO util.ExitUtil: Exiting with status 0
xx/xx/xx xx:xx:xx INFO namenode.NameNode: SHUTDOWN_MSG:
/************************************************************
SHUTDOWN_MSG: Shutting down NameNode at xxx.xxx.xxx.xxx
```

Launch NameNode daemon, DataNode daemon, ResourceManager daemon and NodeManager Daemon.

```bash
$ $HADOOP_HOME/sbin/start-dfs.sh
$ $HADOOP_HOME/sbin/start-yarn.sh
```

Check if the daemons started successfully with the following output.
```bash
$ jps
xxxxx NameNode
xxxxx SecondaryNameNode
xxxxx DataNode
xxxxx NodeManager
xxxxx Jps
xxxxx ResourceManager
```

You can browse the web interface for the NameNode at [http://localhost:50070](http://localhost:50070) and for the ResourceManager at [http://localhost:8080](http://localhost:8080).

## Step 2 --- Launch an example topology

If you set the `--user` flag when running the installation scripts, some example
topologies will be installed in your `~/.heron/examples` directory. You can
launch an example [topology](../concepts/topologies) locally (on your machine)
using the [Heron CLI tool](../operators/heron-cli):

```bash
# Submit ExclamationTopology locally in deactivated mode.
$ heron submit local \
~/.heron/examples/heron-examples.jar \
com.twitter.heron.examples.ExclamationTopology \
ExclamationTopology \
--deploy-deactivated

INFO: Launching topology 'ExclamationTopology'
...
[2016-06-07 16:44:07 -0700] com.twitter.heron.scheduler.local.LocalLauncher INFO: \
For checking the status and logs of the topology, use the working directory \
$HOME/.herondata/topologies/local/${ROLE}/ExclamationTopology # working directory

INFO: Topology 'ExclamationTopology' launched successfully
INFO: Elapsed time: 3.409s.
```

This will *submit* the topology to your locally running Heron cluster but it
won't *activate* the topology. That will be explored in step 5 below.

Note the output shows if the topology has been launched successfully and the working directory.

To check what's under the working directory, run:
```bash
$ ls -al ~/.herondata/topologies/local/${ROLE}/ExclamationTopology
-rw-r--r--   1 username  role     2299 Jun  7 16:44 ExclamationTopology.defn
-rw-r--r--   1 username  role        5 Jun  7 16:44 container_1_exclaim1_1.pid
-rw-r--r--   1 username  role        5 Jun  7 16:44 container_1_word_2.pid
drwxr-xr-x  11 username  role      374 Jun  7 16:44 heron-conf
drwxr-xr-x   4 username  role      136 Dec 31  1969 heron-core
-rwxr-xr-x   1 username  role  2182564 Dec 31  1969 heron-examples.jar
-rw-r--r--   1 username  role        5 Jun  7 16:44 heron-executor-0.pid
-rw-r--r--   1 username  role        0 Jun  6 13:33 heron-executor.stderr
-rw-r--r--   1 username  role    17775 Jun  7 16:44 heron-executor.stdout
-rw-r--r--   1 username  role        5 Jun  7 16:44 heron-shell-0.pid
-rw-r--r--   1 username  role        5 Jun  7 16:44 heron-tmaster.pid
drwxr-xr-x  25 username  role      850 Jun  7 16:44 log-files
-r--r--r--   1 username  role     4506 Jun  8 12:05 metrics.json.metricsmgr-0.0
-rw-r--r--   1 username  role        5 Jun  7 16:44 metricsmgr-0.pid
-r-xr-xr-x   1 username  role      279 Dec 31  1969 release.yaml
-rw-r--r--   1 username  role        5 Jun  7 16:44 stmgr-1.pid
```

All instances' log files can be found in `log-files` under the working directory:

```bash
$ ls -al ~/.herondata/topologies/local/${ROLE}/ExclamationTopology/log-files
total 1018440
-rw-r--r--   1 username  role   94145427 Jun  8 12:06 container_1_exclaim1_1.log.0
-rw-r--r--   1 username  role   75675435 Jun  7 16:44 container_1_word_2.log.0
-rw-r--r--   1 username  role  187401024 Jun  8 12:06 gc.container_1_exclaim1_1.log
-rw-r--r--   1 username  role  136318451 Jun  8 12:06 gc.container_1_word_2.log
-rw-r--r--   1 username  role      11039 Jun  8 11:16 gc.metricsmgr.log
-rw-r--r--   1 username  role        300 Jun  7 16:44 heron-shell.log
-rw-r--r--   1 username  role      29631 Jun  7 16:44 heron-ExclamationTopology-scheduler.log.0
-rw-r--r--   1 username  role    2382215 Jun  7 15:16 heron-stmgr-stmgr-1.username.log.INFO
-rw-r--r--   1 username  role       5976 Jun  7 16:44 heron-tmaster-ExclamationTopology2da9ee6b-c919-4e59-8cb0-20a865f6fd7e.username.log.INFO
-rw-r--r--   1 username  role   12023368 Jun  8 12:06 metricsmgr-0.log.0

```

## Step 3 --- Start Heron Tracker

The [Heron Tracker](../operators/heron-tracker) is a web service that
continuously gathers information about your Heron cluster. You can launch the
tracker by running the `heron-tracker` command (which is already installed):

```bash
$ heron-tracker
... Running on port: 8888
... Using config file: $HOME/.herontools/conf/heron_tracker.yaml
```

You can reach Heron Tracker in your browser at [http://localhost:8888](http://localhost:8888)
and see something like the following upon successful submission of the topology:
![alt tag](/img/heron-tracker.png)

To explore Heron Tracker, please refer to [Heron Tracker Rest API](../operators/heron-tracker-api)

## Step 4 --- Start Heron UI

[Heron UI](../operators/heron-ui) is a user interface that uses Heron Tracker to
provide detailed visual representations of your Heron topologies. To launch
Heron UI:

```bash
$ heron-ui
... Running on port: 8889
... Using tracker url: http://localhost:8888
```

You can open Heron UI in your browser at [http://localhost:8889](http://localhost:8889)
and see something like this upon successful submission of the topology:
![alt tag](/img/heron-ui.png)

To play with Heron UI, please refer to [Heron UI Usage Guide](../developers/ui-guide)
## Step 5 --- Explore topology management commands

In step 2 you submitted a topology to your local cluster. The `heron` CLI tool
also enables you to activate, deactivate, and kill topologies and more.

```bash
$ heron activate local ExclamationTopology
$ heron deactivate local ExclamationTopology
$ heron kill local ExclamationTopology
```

Upon successful actions, a message similar to the following will appear:

```bash
INFO: Successfully activated topology 'ExclamationTopology'
INFO: Elapsed time: 1.980s.
```

For more info on these commands, read about [topology
lifecycles](../concepts/topologies#topology-lifecycle).

To list the available CLI commands, run `heron` by itself:

```bash
usage: heron <command> <options> ...

Available commands:
    activate           Activate a topology
    deactivate         Deactivate a topology
    help               Prints help for commands
    kill               Kill a topology
    restart            Restart a topology
    submit             Submit a topology
    version            Print version of heron-cli

For detailed documentation, go to http://heronstreaming.io
```

To invoke help output for a command, run `heron help COMMAND`. Here's an
example:

```bash
$ heron help submit
usage: heron submit [options] cluster/[role]/[environ] topology-file-name topology-class-name [topology-args]

Required arguments:
  cluster/[role]/[env]  Cluster, role, and environ to run topology
  topology-file-name    Topology jar/tar/zip file
  topology-class-name   Topology class name

Optional arguments:
  --config-path (a string; path to cluster config; default: "$HOME/.heron/conf")
  --config-property (key=value; a config key and its value; default: [])
  --deploy-deactivated (a boolean; default: "false")
  -D DEFINE             Define a system property to pass to java -D when
                        running main.
  --verbose (a boolean; default: "false")
```

## Step 6 --- Explore other example topologies

The source code for the example topologies can be found
[on
GitHub]({{% githubMaster %}}/heron/examples/src/java/com/twitter/heron/examples).
The included example topologies:

* `AckingTopology.java` --- A topology with acking enabled.
* `ComponentJVMOptionsTopology.java` --- A topology that supplies JVM options
  for each component.
* `CustomGroupingTopology.java` --- A topology that implements custom grouping.
* `ExclamationTopology.java` --- A spout that emits random words to a bolt that
  then adds an exclamation mark.
* `MultiSpoutExclamationTopology.java` --- a topology with multiple spouts.
* `MultiStageAckingTopology.java` --- A three-stage topology. A spout emits to a
  bolt that then feeds to another bolt.
* `TaskHookTopology.java` --- A topology that uses a task hook to subscribe to
   event notifications.

## Troubleshooting
In case of any issues, please refer to [Quick Start Troubleshooting](../getting-started-troubleshooting).

### Next Steps

* [Upgrade Storm topologies](../upgrade-storm-to-heron) with simple `pom.xml`
  changes
* [Deploy topologies](../operators/deployment) in clustered, scheduler-driven
  environments (such as on [Aurora](../operators/deployment/schedulers/aurora)
  and [locally](../operators/deployment/schedulers/local))
* [Develop topologies](../concepts/architecture) for Heron
