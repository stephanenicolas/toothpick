#!/bin/bash
set -ev
bundle exec rake:units
if [ "${TRAVIS_BRANCH}" = "master" ]; then
    git push -f origin integration-tests
fi
