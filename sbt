#!/bin/bash
#------------------------------------------------------------------
# sbt driver script. 
#------------------------------------------------------------------

jettyport=
jmxport=

function showhelp() {
  cat <<EOF
usage:
  ./sbt [-h|--help] [sbt_args] [actions]
where:
  -h | --help       Show this help message.
  sbt_args actions  Any other arguments are passed to SBT itself, including optional "actions" to invoke.
EOF
}

while [ $# -ne 0 ]
do
  case $1 in
    help|-h*|--h*)
      showhelp
      exit 0
      ;;
    *)
      break
      ;;
  esac
  shift
done

env java $JAVA_OPTIONS -jar ./misc/sbt-launch-0.7.4.jar "$@"
