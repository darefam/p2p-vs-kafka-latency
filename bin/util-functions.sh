#!/bin/bash

retry() {
    local -r -i max_wait="$1"; shift
    local -r cmd="$1"; shift
    local -r args="$@"

    local -i sleep_interval=5
    local -i curr_wait=0

    until $cmd $args
    do
        if (( curr_wait >= max_wait ))
        then
            echo "ERROR: Failed after $curr_wait seconds. Please troubleshoot and run again."
            return 1
        else
            printf "."
            curr_wait=$((curr_wait+sleep_interval))
            sleep $sleep_interval
        fi
    done

    PRETTY_PASS="\e[32m✔ \e[0m"
    printf "${PRETTY_PASS}%s\n\n"
}

validateOption() {
  local item="$1"
  local valid_list="$2"
  if ! [[ $valid_list =~ (^|[[:space:]])"$item"($|[[:space:]]) ]] ; then
    echo 'Wrong parameter, '$item'! Please specify either one of these ['"$valid_list"'].'
    exit 1
  fi
}

checkMode() {
  local mode="$1"
  if [ -z "$mode" ]
  then
     echo 'Please specify the channel of communication between services ['"$VALID_CHANNEL_MODES"']!'
     exit 1
  fi
}
