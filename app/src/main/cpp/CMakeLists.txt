cmake_minimum_required(VERSION 3.22.1)

project("bitefast_native")

add_library(
    bitefast_native
    SHARED
    native-lib.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    bitefast_native
    ${log-lib}
)
