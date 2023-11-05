
```agsl
./run.sh --id 1 --hosts ./bin/hosts --output ./bin/output/1.output ./bin/configs/perfect-links.config
./stress.py perfect -r ./run.sh -l ./bin/ -p 3 -m 5
```
