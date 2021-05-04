#!/usr/bin/env bash
set -x

# If this is a main build then lay out signing information
if [ "$TRAVIS_BRANCH" = 'main' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  openssl aes-256-cbc -K $encrypted_6b1cbeda7dc5_key -iv $encrypted_6b1cbeda7dc5_iv -in .travis/signingkey.asc.enc -out .travis/signingkey.asc -d
  gpg --version
  gpg --fast-import .travis/signingkey.asc
  rm .travis/signingkey.asc*
fi