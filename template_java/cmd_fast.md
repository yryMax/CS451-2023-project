
```agsl
./run.sh --id 1 --hosts ./bin/hosts --output ./bin/output/1.output ./bin/configs/perfect-links.config

../tools/stress.py perfect -r ./run.sh -l ./bin/ -p 3 -m 5

python3 ../tools/validate_fifo.py --proc_num 3 bin/proc01.output bin/proc02.output bin/proc03.output

```
