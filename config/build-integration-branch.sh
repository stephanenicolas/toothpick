#!/bin/bash
set -ev

if [ "${TRAVIS_BRANCH}" = "master" ]; then
    git push -f origin integration-tests
fi
