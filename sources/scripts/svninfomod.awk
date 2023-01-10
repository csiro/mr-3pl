BEGIN {
    first = 1;
}
{
    i = index($0, ": ");
    if (i < 2) next;
    k = substr($0, 1, i - 1);
    if (k !~ /^(Last Changed |URL)/) next;
    sub(/^Last Changed /, "", k);
    v = substr($0, i + 2, length - (i + 1));
    sub(/^[ \t]*/, "", v);
    sub(/[ \t]*$/, "", v);
    if (tolower(k) ~ /date/) sub(/[ \t]*\([^\(\)]*\)/, "", v);
    if (first == 0) printf("|");
    printf("%s%s=%s", p, toupper(k), v);
    first = 0;
}

