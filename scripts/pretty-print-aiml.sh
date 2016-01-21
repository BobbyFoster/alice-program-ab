echo 'not pretty-printing aiml files...'
#find . -type f -name "*.aiml" | xargs -I % sh -c 'echo %; tidy -xml -i -q -m -w 240 %'
